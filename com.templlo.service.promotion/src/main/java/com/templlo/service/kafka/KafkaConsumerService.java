package com.templlo.service.kafka;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.templlo.service.coupon.dto.CouponIssueRequestDto;
import com.templlo.service.coupon.dto.CouponUseResponseDto;
import com.templlo.service.coupon.dto.TicketApplyRequestDto;
import com.templlo.service.coupon.entity.Coupon;
import com.templlo.service.coupon.repository.CouponRepository;
import com.templlo.service.coupon.service.CouponService;
import com.templlo.service.kafka.dto.ReservationCreatedEvent;
import com.templlo.service.promotion.dto.PromotionCreatedEvent;
import com.templlo.service.promotion.entity.Promotion;
import com.templlo.service.promotion.repository.PromotionRepository;
import com.templlo.service.user_coupon.entity.UserCoupon;
import com.templlo.service.user_coupon.repository.UserCouponRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerService {

	private final ObjectMapper objectMapper;
	private final CouponRepository couponRepository;
	private final PromotionRepository promotionRepository;
	private final UserCouponRepository userCouponRepository;
	private final KafkaProducerService kafkaProducerService;
	private final CouponService couponService;

	/**
	 * 티켓 신청 요청 처리
	 */
	@KafkaListener(topics = "ticket-application-queue", groupId = "ticket-service")
	public void consumeTicketRequest(String message) {
		try {
			TicketApplyRequestDto ticketApplyRequest = objectMapper.readValue(message, TicketApplyRequestDto.class);

			log.info("티켓 신청 요청 수신: {}", ticketApplyRequest);

			// 필요한 추가 처리 로직
		} catch (Exception e) {
			log.error("티켓 신청 요청 처리 실패: {}", e.getMessage(), e);
		}
	}

	/**
	 * 프로모션 생성 이벤트 처리
	 */
	@KafkaListener(topics = "promotion-created-topic", groupId = "promotion-service")
	public void handlePromotionCreated(PromotionCreatedEvent event, Acknowledgment acknowledgment) {
		try {
			log.info("프로모션 생성 이벤트 수신: {}", event);

			// 프로모션 존재 여부 확인
			Promotion promotion = promotionRepository.findById(event.promotionId())
				.orElseThrow(() -> new IllegalArgumentException("프로모션을 찾을 수 없습니다."));

			// 이미 쿠폰이 생성되었는지 확인
			if (couponRepository.existsByPromotion_PromotionId(event.promotionId())) {
				log.warn("이미 처리된 프로모션 ID: {} - 쿠폰 생성 작업 중단", event.promotionId());
				acknowledgment.acknowledge(); // 메시지 확인 (ACK)
				return;
			}

			// 쿠폰 생성
			for (int i = 0; i < event.totalCoupons(); i++) {
				Coupon coupon = Coupon.builder()
					.promotion(promotion)
					.type(event.couponType())
					.status("AVAILABLE")
					.createdBy("SYSTEM") // 시스템 사용자로 설정
					.updatedBy("SYSTEM")
					.build();
				couponRepository.save(coupon);
			}

			log.info("프로모션 ID {}의 쿠폰 생성 완료", event.promotionId());
			acknowledgment.acknowledge(); // 메시지 확인 (ACK)

		} catch (Exception e) {
			log.error("프로모션 처리 중 오류 발생: {}", e.getMessage(), e);
			// 예외 발생 시 메시지 Ack를 호출하지 않아 Kafka가 재시도할 수 있도록 함
		}
	}

	private void processPromotionEvent(PromotionCreatedEvent event) {
		try {
			// 프로모션 ID와 정보를 기반으로 쿠폰 생성
			Promotion promotion = promotionRepository.findById(event.promotionId())
				.orElseThrow(() -> new IllegalArgumentException("프로모션을 찾을 수 없습니다."));

			for (int i = 0; i < event.totalCoupons(); i++) {
				Coupon coupon = Coupon.builder()
					.promotion(promotion)
					.type(event.couponType())
					.status("AVAILABLE")
					.build();
				couponRepository.save(coupon);
			}

			log.info("프로모션 ID {}의 쿠폰 생성 완료", event.promotionId());
		} catch (Exception e) {
			log.error("프로모션 처리 중 오류 발생: {}", e.getMessage(), e);
		}
	}

	/**
	 * 쿠폰 발급 요청 이벤트 처리
	 */
	@KafkaListener(topics = "coupon-issue-topic", groupId = "coupon-service")
	public void handleCouponIssueRequest(CouponIssueRequestDto request,
		@Header(name = "X-Login-Id", required = false) String loginId, Acknowledgment acknowledgment) {

		try {
			log.info("쿠폰 발급 요청 이벤트 수신: {}, X-Login-Id: {}", request, loginId);

			if (loginId == null || loginId.isEmpty()) {
				log.warn("X-Login-Id 헤더가 누락되었습니다. 기본값을 사용합니다.");
				loginId = "UNKNOWN";
			}

			// 쿠폰 조회 및 상태 확인
			Coupon coupon = couponRepository.findById(request.couponId())
				.orElseThrow(() -> new IllegalArgumentException("유효하지 않은 쿠폰 ID입니다. 쿠폰 ID: " + request.couponId()));

			log.info("쿠폰 상태: {}, 쿠폰 ID: {}", coupon.getStatus(), coupon.getCouponId());

			if (!"AVAILABLE".equalsIgnoreCase(coupon.getStatus())) {
				log.warn("쿠폰 상태가 AVAILABLE이 아님: {}", coupon.getStatus());
				throw new IllegalStateException("쿠폰이 이미 발급되었거나 사용할 수 없습니다.");
			}

			// 쿠폰 상태 업데이트
			coupon.updateStatus("ISSUED");
			couponRepository.save(coupon);

			// UserCoupon 저장
			UserCoupon userCoupon = UserCoupon.builder()
				.userId(request.userId())
				.userLoginId(loginId)
				.coupon(coupon)
				.status("ISSUED")
				.issuedAt(LocalDateTime.now())
				.createdBy(loginId)
				.updatedBy(loginId)
				.build();
			userCouponRepository.save(userCoupon);

			// // 성공 메시지 전송
			// kafkaProducerService.sendMessageWithCallback("coupon-issued-response-topic", new CouponIssuedResponseEvent(
			// 	request.userId(),
			// 	"SUCCESS",
			// 	coupon.getCouponId()
			// ));

			log.info("쿠폰 발급 처리 완료: {}", coupon.getCouponId());

			acknowledgment.acknowledge();
		} catch (IllegalStateException e) {
			log.error("쿠폰 발급 실패: {}", e.getMessage());

			// // 실패 메시지 전송
			// kafkaProducerService.sendMessageWithCallback("coupon-issued-response-topic", new CouponIssuedResponseEvent(
			// 	request.userId(),
			// 	"FAILURE",
			// 	null
			// ));
			acknowledgment.acknowledge();

		} catch (Exception e) {
			log.error("쿠폰 발급 요청 처리 중 예기치 않은 오류 발생: {}", e.getMessage(), e);
			acknowledgment.acknowledge();

			// // 예외 메시지 전송
			// kafkaProducerService.sendMessageWithCallback("coupon-issued-response-topic", new CouponIssuedResponseEvent(
			// 	request.userId(),
			// 	"FAILURE",
			// 	null
			// ));
		}
	}

	@KafkaListener(topics = "review-reward-coupon", groupId = "coupon-service")
	public void handleReviewRewardCoupon(String message, Acknowledgment acknowledgment) {
		try {
			log.info("리뷰 보상 쿠폰 이벤트 수신: {}", message);

			// 메시지를 User ID로 변환
			UUID userId = objectMapper.readValue(message, UUID.class);

			// 유저에게 레벨 업 쿠폰 발급
			couponService.issueLevelUpCoupon(userId);

			// 메시지 처리 완료 후 Kafka 오프셋 커밋
			acknowledgment.acknowledge();
			log.info("레벨 업 쿠폰 발급 완료: userId={}", userId);
		} catch (Exception e) {
			log.error("리뷰 보상 쿠폰 이벤트 처리 중 오류 발생: {}", e.getMessage(), e);
			// 예외 발생 시 메시지를 다시 처리할 수 있도록 Ack를 호출하지 않음
		}
	}

	/**
	 * 예약 신청 메시지 처리 (쿠폰 사용 처리)
	 */
	@KafkaListener(topics = "reservation-created", groupId = "reservation-created-coupon")
	public void handleReservationCreated(String message, Acknowledgment acknowledgment) {
		try {
			log.info("예약 신청 이벤트 수신: {}", message);

			// 메시지 역직렬화
			ReservationCreatedEvent event = objectMapper.readValue(message, ReservationCreatedEvent.class);

			// 쿠폰 사용 처리
			CouponUseResponseDto response = couponService.useCoupon(
				UUID.fromString(event.couponId()),
				UUID.fromString(event.programId()),
				event.programDate()
			);

			if ("SUCCESS".equals(response.status())) {
				log.info("쿠폰 사용 성공: CouponId={}, FinalPrice={}", event.couponId(), response.finalPrice());
			} else {
				log.warn("쿠폰 사용 실패: CouponId={}, Reason={}", event.couponId(), response.message());
			}

			// Kafka 메시지 확인 (ACK)
			acknowledgment.acknowledge();

		} catch (Exception e) {
			log.error("예약 신청 메시지 처리 중 오류 발생: {}", e.getMessage(), e);
			// 예외 발생 시 메시지를 다시 처리할 수 있도록 Ack를 호출하지 않음
		}
	}

	// private <T> T retryOnFailure(Supplier<T> task, int maxRetries, long delayInMillis) throws Exception {
	// 	int attempt = 0;
	// 	while (attempt < maxRetries) {
	// 		try {
	// 			return task.get();
	// 		} catch (Exception e) {
	// 			attempt++;
	// 			if (attempt >= maxRetries) {
	// 				throw e;
	// 			}
	// 			Thread.sleep(delayInMillis);
	// 		}
	// 	}
	// 	throw new IllegalStateException("재시도 로직이 실패했습니다.");
	// }
}
