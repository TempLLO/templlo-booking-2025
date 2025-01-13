package com.templlo.service.coupon.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.templlo.service.common.client.ProgramFeignClient;
import com.templlo.service.common.dto.DetailProgramResponse;
import com.templlo.service.common.response.ApiResponse;
import com.templlo.service.coupon.dto.CouponDeleteResponseDto;
import com.templlo.service.coupon.dto.CouponIssueRequestDto;
import com.templlo.service.coupon.dto.CouponIssueResponseDto;
import com.templlo.service.coupon.dto.CouponStatusResponseDto;
import com.templlo.service.coupon.dto.CouponTransferResponseDto;
import com.templlo.service.coupon.dto.CouponUpdateRequestDto;
import com.templlo.service.coupon.dto.CouponUpdateResponseDto;
import com.templlo.service.coupon.dto.CouponUseResponseDto;
import com.templlo.service.coupon.dto.CouponValidationResponseDto;
import com.templlo.service.coupon.entity.Coupon;
import com.templlo.service.coupon.repository.CouponRepository;
import com.templlo.service.kafka.KafkaProducerService;
import com.templlo.service.promotion.entity.Promotion;
import com.templlo.service.promotion.repository.PromotionRepository;
import com.templlo.service.user_coupon.entity.UserCoupon;
import com.templlo.service.user_coupon.repository.UserCouponRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponService {

	private final CouponRepository couponRepository;
	private final UserCouponRepository userCouponRepository;
	private final PromotionRepository promotionRepository;
	private final RedissonClient redissonClient;
	private final KafkaProducerService kafkaProducerService;
	private final RedisTemplate<String, Object> redisTemplate; // RedisTemplate 주입
	private final ProgramFeignClient programFeignClient;

	@Transactional
	public CouponIssueResponseDto issueCoupon(UUID promotionId, String gender, UUID userId, String userLoginId) {
		log.debug("issueCoupon 호출: userLoginId={}", userLoginId);

		String lockKey = "promotion:lock:" + promotionId;
		RLock lock = redissonClient.getLock(lockKey);

		try {
			if (!lock.tryLock(10, 5, TimeUnit.SECONDS)) {
				throw new IllegalStateException("현재 쿠폰 발급 중입니다. 잠시 후 다시 시도해주세요.");
			}

			Promotion promotion = promotionRepository.findById(promotionId)
				.orElseThrow(() -> new IllegalArgumentException("유효하지 않은 프로모션 ID입니다."));

			if (userCouponRepository.existsByUserIdAndCoupon_Promotion_PromotionId(userId, promotionId)) {
				throw new IllegalStateException("이미 해당 프로모션에 참여한 사용자입니다.");
			}

			initializeRedisPromotionCounters(promotionId, promotion.getTotalCoupons());

			boolean success = decrementRemainingCouponsAtomically(promotionId);

			if (!success) {
				throw new IllegalStateException("남은 쿠폰 수량이 부족합니다.");
			}

			Coupon coupon = fetchAndMarkCoupon(promotion, gender);

			CouponIssueRequestDto requestDto = new CouponIssueRequestDto(
				coupon.getCouponId(),
				userId,
				promotionId,
				gender,
				"Coupon issued successfully."
			);

			// Kafka Producer 호출
			kafkaProducerService.sendCouponIssueRequest(requestDto, userLoginId);

			return new CouponIssueResponseDto("SUCCESS", coupon.getCouponId(), "쿠폰이 발급되었습니다.");
		} catch (Exception e) {
			log.error("쿠폰 발급 중 오류 발생: userId={}, userLoginId={}, error={}", userId, userLoginId, e.getMessage(), e);
			throw new IllegalStateException("쿠폰 발급 중 오류가 발생했습니다.", e);
		} finally {
			if (lock.isHeldByCurrentThread()) {
				lock.unlock();
			}
		}
	}

	private Coupon fetchAndMarkCoupon(Promotion promotion, String gender) {
		if (gender != null) {
			return couponRepository.findFirstByPromotionAndGenderAndStatus(promotion, gender, "AVAILABLE")
				.orElseThrow(() -> new IllegalStateException("해당 성별에 사용 가능한 쿠폰이 없습니다."));
		} else {
			return couponRepository.findFirstByPromotionAndStatus(promotion, "AVAILABLE")
				.orElseThrow(() -> new IllegalStateException("사용 가능한 쿠폰이 없습니다."));
		}
	}

	@Transactional
	public CouponUpdateResponseDto updateCoupon(UUID couponId, CouponUpdateRequestDto requestDto) {
		// 쿠폰 조회
		Coupon coupon = couponRepository.findById(couponId)
			.orElseThrow(() -> new IllegalArgumentException("유효하지 않은 쿠폰 ID입니다."));

		// 상태 업데이트
		coupon.updateStatus(requestDto.status());
		couponRepository.save(coupon);

		// 응답 생성
		return new CouponUpdateResponseDto(
			"SUCCESS",
			coupon.getCouponId().toString(),
			"쿠폰 상태가 성공적으로 수정되었습니다."
		);
	}

	@Transactional
	public CouponUpdateResponseDto deleteCoupon(UUID couponId, String userId) {
		// 쿠폰 조회
		Coupon coupon = couponRepository.findById(couponId)
			.orElseThrow(() -> new IllegalArgumentException("유효하지 않은 쿠폰 ID입니다."));

		// Soft Delete 처리
		coupon.delete(userId);
		coupon.updateStatus("DELETED");
		couponRepository.save(coupon);

		// 응답 생성
		return new CouponUpdateResponseDto(
			"SUCCESS",
			coupon.getCouponId().toString(),
			"쿠폰이 성공적으로 삭제(비활성화)되었습니다."
		);
	}

	@Cacheable(value = "couponStatus", key = "#promotionId", unless = "#result == null")
	@Transactional(readOnly = true)
	public CouponStatusResponseDto getCouponStatus(UUID promotionId) {
		Promotion promotion = promotionRepository.findById(promotionId)
			.orElseThrow(() -> new IllegalArgumentException("유효하지 않은 프로모션 ID입니다."));

		return new CouponStatusResponseDto(
			promotion.getPromotionId().toString(),
			promotion.getTotalCoupons(),
			promotion.getIssuedCoupons(),
			promotion.getRemainingCoupons()
		);
	}

	@Transactional(readOnly = true)
	public CouponValidationResponseDto validateCoupon(UUID couponId) {
		// 1. 쿠폰 조회
		Coupon coupon = couponRepository.findById(couponId)
			.orElseThrow(() -> new IllegalArgumentException("유효하지 않은 쿠폰 ID입니다."));

		// 2. 쿠폰 상태 확인
		if (coupon.getStatus().equals("AVAILABLE")) {
			return new CouponValidationResponseDto(true, "쿠폰이 유효합니다.");
		} else if (coupon.getStatus().equals("ISSUED")) {
			return new CouponValidationResponseDto(false, "쿠폰이 이미 발급되었습니다.");
		} else if (coupon.getStatus().equals("EXPIRED")) {
			return new CouponValidationResponseDto(false, "쿠폰이 만료되었습니다.");
		}

		return new CouponValidationResponseDto(false, "알 수 없는 쿠폰 상태입니다.");
	}

	@Transactional
	public CouponUseResponseDto useCoupon(UUID couponId, UUID programId, LocalDate programDate) {
		// 1. 쿠폰 조회
		Coupon coupon = couponRepository.findById(couponId)
			.orElseThrow(() -> new IllegalArgumentException("유효하지 않은 쿠폰 ID입니다."));

		// 2. 프로그램 정보 조회 (Feign Client 사용)
		ApiResponse<DetailProgramResponse> programResponse = programFeignClient.getProgram(programId, programDate);

		if (programResponse == null || programResponse.data() == null) {
			throw new IllegalStateException("프로그램 정보를 가져올 수 없습니다.");
		}

		DetailProgramResponse program = programResponse.data();

		// 3. 프로그램 타입 검증
		if ("BLIND_DATE".equals(program.type()) && !"ADVANCED_TICKET".equals(coupon.getType())) {
			return new CouponUseResponseDto("FAILURE", "이 프로그램에서는 ADVANCED_TICKET 쿠폰만 사용할 수 있습니다.");
		}

		// 4. 쿠폰 상태 검증
		if (!"ISSUED".equals(coupon.getStatus())) {
			String message = switch (coupon.getStatus()) {
				case "AVAILABLE" -> "쿠폰이 발급되지 않았습니다.";
				case "EXPIRED" -> "쿠폰이 만료되었습니다.";
				case "USED" -> "쿠폰이 이미 사용되었습니다.";
				default -> "알 수 없는 쿠폰 상태입니다.";
			};
			return new CouponUseResponseDto("FAILURE", message);
		}

		// 5. 할인 금액 계산
		BigDecimal discountAmount = BigDecimal.ZERO;
		if ("PERCENTAGE".equals(coupon.getDiscountType())) {
			discountAmount = program.programFee().multiply(coupon.getValue().divide(BigDecimal.valueOf(100)));
		} else if ("AMOUNT".equals(coupon.getDiscountType())) {
			discountAmount = coupon.getValue();
		}

		// 할인 금액이 프로그램 금액을 초과하지 않도록 제한
		if (discountAmount.compareTo(program.programFee()) > 0) {
			discountAmount = program.programFee();
		}

		// 최종 금액 계산
		BigDecimal finalPrice = program.programFee().subtract(discountAmount);

		// 6. 쿠폰 상태 업데이트
		coupon.updateStatus("USED");
		couponRepository.save(coupon);

		// 7. 응답 생성
		return new CouponUseResponseDto("SUCCESS", "쿠폰이 사용되었습니다.", finalPrice);
	}

	@Transactional
	public CouponTransferResponseDto transferCoupon(UUID couponId, UUID toUserId) {
		// 1. `UserCoupon`에서 쿠폰 조회
		UserCoupon userCoupon = userCouponRepository.findByCouponId(couponId)
			.orElseThrow(() -> new IllegalArgumentException("유효하지 않은 쿠폰 ID입니다."));

		// 2. 쿠폰 상태 확인
		if ("USED".equals(userCoupon.getStatus())) {
			throw new IllegalStateException("사용된 쿠폰은 양도할 수 없습니다.");
		}

		// 3. 쿠폰 양도 처리
		userCoupon = userCoupon.toBuilder()
			.userId(toUserId) // 새로운 사용자 ID로 변경
			.fromUserId(userCoupon.getUserId()) // 기존 사용자 ID 기록
			.transferredAt(LocalDateTime.now()) // 양도 날짜 기록
			.build();

		userCouponRepository.save(userCoupon);

		// 4. 응답 생성
		return new CouponTransferResponseDto(
			"SUCCESS",
			"쿠폰이 성공적으로 양도되었습니다."
		);
	}

	public Page<UserCoupon> getUserCoupons(UUID userId, UUID promotionId, String status, int page, int size) {
		Pageable pageable = PageRequest.of(page, size);

		// `UserCouponRepository`를 통해 사용자 ID, 프로모션 ID 및 상태로 필터링
		if (promotionId != null && status != null) {
			return userCouponRepository.findByUserIdAndPromotionIdAndStatus(userId, promotionId, status, pageable);
		} else if (promotionId != null) {
			return userCouponRepository.findByUserIdAndPromotionId(userId, promotionId, pageable);
		} else if (status != null) {
			return userCouponRepository.findByUserIdAndStatus(userId, status, pageable);
		} else {
			return userCouponRepository.findByUserId(userId, pageable);
		}
	}

	@Transactional(readOnly = true)
	public Page<UserCoupon> getMyCoupons(String loginId, UUID promotionId, String status, int page, int size) {
		Pageable pageable = PageRequest.of(page, size);

		// `UserCouponRepository`를 통해 로그인 ID를 기반으로 본인 쿠폰 조회
		if (promotionId != null && status != null) {
			return userCouponRepository.findByUserLoginIdAndPromotionIdAndStatus(loginId, promotionId, status,
				pageable);
		} else if (promotionId != null) {
			return userCouponRepository.findByUserLoginIdAndPromotionId(loginId, promotionId, pageable);
		} else if (status != null) {
			return userCouponRepository.findByUserLoginIdAndStatus(loginId, status, pageable);
		} else {
			return userCouponRepository.findByUserLoginId(loginId, pageable);
		}
	}

	@Transactional
	public CouponDeleteResponseDto deleteUserCoupon(UUID userId, UUID couponId) {
		// 1. `UserCoupon`에서 사용자 ID와 쿠폰 ID로 엔티티 조회
		UserCoupon userCoupon = userCouponRepository.findByUserIdAndCouponId(userId, couponId)
			.orElseThrow(() -> new IllegalArgumentException("해당 사용자가 가진 유효한 쿠폰이 없습니다."));

		// 2. 엔티티 삭제
		userCouponRepository.delete(userCoupon);

		// 3. 응답 생성
		return new CouponDeleteResponseDto(
			"SUCCESS",
			"쿠폰이 성공적으로 삭제되었습니다."
		);
	}

	@CacheEvict(value = "couponStatus", key = "#promotionId")
	@Transactional
	public void evictCouponStatusCache(UUID promotionId) {
		// 이 메서드는 캐시 무효화를 트리거하기 위해 사용됩니다.
	}

	private void initializeRedisPromotionCounters(UUID promotionId, int totalCoupons) {
		if (redisTemplate.opsForValue().get("promotion:" + promotionId + ":remaining") == null) {
			redisTemplate.opsForValue().set("promotion:" + promotionId + ":remaining", (long)totalCoupons);
		}
		if (redisTemplate.opsForValue().get("promotion:" + promotionId + ":issued") == null) {
			redisTemplate.opsForValue().set("promotion:" + promotionId + ":issued", 0L);
		}
	}

	private boolean decrementRemainingCouponsAtomically(UUID promotionId) {
		String luaScript =
			"local remaining = redis.call('GET', KEYS[1]) " +
				"if tonumber(remaining) > 0 then " +
				"    redis.call('DECR', KEYS[1]) " +
				"    return 1 " +
				"else " +
				"    return 0 " +
				"end";

		// RedisScript<Long> 객체 생성
		RedisScript<Long> redisScript = RedisScript.of(luaScript, Long.class);

		// Lua 스크립트 실행
		Long result = redisTemplate.execute(
			redisScript,
			List.of("promotion:" + promotionId + ":remaining"),
			new Object[0] // 추가적인 ARGV 값이 없을 경우 빈 배열 사용
		);

		System.out.println("Lua script execution result: " + result); // 디버깅 로그
		return result != null && result == 1;
	}

	@Transactional
	public void issueLevelUpCoupon(UUID userId) {
		// 1. 쿠폰 생성
		Coupon coupon = Coupon.builder()
			.type("LEVEL_UP")
			.status("ISSUED")
			.createdBy("SYSTEM") // 시스템 사용자로 설정
			.build();
		coupon = couponRepository.save(coupon);

		// 2. 유저 쿠폰 발급
		UserCoupon userCoupon = UserCoupon.builder()
			.userId(userId)
			.coupon(coupon)
			.status("ISSUED")
			.issuedAt(LocalDateTime.now())
			.createdBy("SYSTEM")
			.updatedBy("SYSTEM")
			.build();
		userCouponRepository.save(userCoupon);

		log.info("레벨 업 쿠폰 발급 완료: userId={}", userId);
	}

}