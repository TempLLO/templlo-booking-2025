// package com.templlo.service.kafka;
//
// import java.time.LocalDateTime;
//
// import org.springframework.kafka.annotation.KafkaListener;
// import org.springframework.messaging.handler.annotation.Header;
// import org.springframework.stereotype.Component;
//
// import com.templlo.service.coupon.dto.CouponIssueRequestDto;
// import com.templlo.service.coupon.dto.CouponIssuedResponseEvent;
// import com.templlo.service.coupon.entity.Coupon;
// import com.templlo.service.coupon.repository.CouponRepository;
// import com.templlo.service.user_coupon.entity.UserCoupon;
// import com.templlo.service.user_coupon.repository.UserCouponRepository;
//
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
//
// @Component
// @RequiredArgsConstructor
// @Slf4j
// public class CouponIssueConsumer {
//
// 	private final CouponRepository couponRepository;
// 	private final UserCouponRepository userCouponRepository;
// 	private final KafkaProducerService kafkaProducerService;
//
// 	@KafkaListener(topics = "coupon-issue-topic", groupId = "coupon-service")
// 	public void handleCouponIssueRequest(CouponIssueRequestDto request,
// 		@Header(name = "X-Login-Id", required = false) String loginId) {
// 		try {
// 			log.info("쿠폰 발급 요청 이벤트 수신: {}, X-Login-Id: {}", request, loginId);
//
// 			if (loginId == null || loginId.isEmpty()) {
// 				log.warn("X-Login-Id 헤더가 누락되었습니다. 기본값을 사용합니다.");
// 				loginId = "UNKNOWN";
// 			}
//
// 			Coupon coupon = couponRepository.findById(request.couponId())
// 				.orElseThrow(() -> new IllegalArgumentException("유효하지 않은 쿠폰 ID입니다."));
//
// 			if (!"AVAILABLE".equalsIgnoreCase(coupon.getStatus())) {
// 				throw new IllegalStateException("쿠폰이 이미 발급되었거나 사용할 수 없습니다.");
// 			}
//
// 			coupon.updateStatus("ISSUED");
// 			couponRepository.save(coupon);
//
// 			UserCoupon userCoupon = UserCoupon.builder()
// 				.userId(request.userId())
// 				.userLoginId(loginId)
// 				.coupon(coupon)
// 				.status("ISSUED")
// 				.issuedAt(LocalDateTime.now())
// 				.build();
// 			userCouponRepository.save(userCoupon);
//
// 			kafkaProducerService.sendCouponIssueResponse(new CouponIssuedResponseEvent(
// 				request.userId(),
// 				"SUCCESS",
// 				coupon.getCouponId()
// 			));
//
// 			log.info("쿠폰 발급 처리 완료: {}", coupon.getCouponId());
// 		} catch (Exception e) {
// 			log.error("쿠폰 발급 요청 처리 실패: {}", e.getMessage(), e);
//
// 			kafkaProducerService.sendCouponIssueResponse(new CouponIssuedResponseEvent(
// 				request.userId(),
// 				"FAILURE",
// 				null
// 			));
// 		}
// 	}
// }
