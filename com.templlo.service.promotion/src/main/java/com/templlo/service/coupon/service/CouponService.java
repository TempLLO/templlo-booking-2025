package com.templlo.service.coupon.service;

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

import com.templlo.service.coupon.dto.CouponDeleteResponseDto;
import com.templlo.service.coupon.dto.CouponIssueResponseDto;
import com.templlo.service.coupon.dto.CouponStatusEvent;
import com.templlo.service.coupon.dto.CouponStatusResponseDto;
import com.templlo.service.coupon.dto.CouponTransferResponseDto;
import com.templlo.service.coupon.dto.CouponUpdateRequestDto;
import com.templlo.service.coupon.dto.CouponUpdateResponseDto;
import com.templlo.service.coupon.dto.CouponUseResponseDto;
import com.templlo.service.coupon.dto.CouponValidationResponseDto;
import com.templlo.service.coupon.entity.Coupon;
import com.templlo.service.coupon.repository.CouponRepository;
import com.templlo.service.promotion.entity.Promotion;
import com.templlo.service.promotion.repository.PromotionRepository;
import com.templlo.service.user_coupon.entity.UserCoupon;
import com.templlo.service.user_coupon.repository.UserCouponRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CouponService {

	private final CouponRepository couponRepository;
	private final UserCouponRepository userCouponRepository;
	private final PromotionRepository promotionRepository;
	private final RedissonClient redissonClient;
	private final KafkaProducerService kafkaProducerService;
	private final RedisTemplate<String, Object> redisTemplate; // RedisTemplate 주입

	@Transactional
	public CouponIssueResponseDto issueCoupon(UUID promotionId, String gender) {
		String lockKey = "promotion:lock:" + promotionId.toString();
		RLock lock = redissonClient.getLock(lockKey);

		try {
			if (!lock.tryLock(10, 5, TimeUnit.SECONDS)) {
				throw new IllegalStateException("현재 쿠폰 발급 중입니다. 잠시 후 다시 시도해주세요.");
			}

			// Promotion 조회
			Promotion promotion = promotionRepository.findById(promotionId)
				.orElseThrow(() -> new IllegalArgumentException("유효하지 않은 프로모션 ID입니다."));

			// Redis 초기화
			initializeRedisPromotionCounters(promotionId, promotion.getTotalCoupons());

			// Redis에서 원자적 감소 연산 수행 (Lua 스크립트 활용)
			boolean success = decrementRemainingCouponsAtomically(promotionId);
			if (!success) {
				throw new IllegalStateException("남은 쿠폰 수량이 부족합니다.");
			}

			// 쿠폰 조회 및 상태 업데이트
			Coupon coupon = fetchAndMarkCoupon(promotionId, gender);

			// Redis에서 발급 수량 증가
			Long issuedCount = redisTemplate.opsForValue().increment("promotion:" + promotionId + ":issued");

			// Kafka 이벤트 발행
			kafkaProducerService.sendCouponStatusEvent(new CouponStatusEvent(
				promotionId.toString(),
				issuedCount.intValue(),
				promotion.getTotalCoupons() - issuedCount.intValue(),
				promotion.getTotalCoupons()
			));

			return new CouponIssueResponseDto("SUCCESS", coupon.getCouponId(), "쿠폰이 발급되었습니다.");
		} catch (Exception e) {
			throw new IllegalStateException("쿠폰 발급 중 오류가 발생했습니다.", e);
		} finally {
			if (lock.isHeldByCurrentThread()) {
				lock.unlock();
			}
		}
	}

	private Coupon fetchAndMarkCoupon(UUID promotionId, String gender) {
		Promotion promotion = promotionRepository.findById(promotionId)
			.orElseThrow(() -> new IllegalArgumentException("유효하지 않은 프로모션 ID입니다."));

		return (gender != null) ?
			couponRepository.findFirstByPromotionAndGenderAndStatus(promotion, gender, "AVAILABLE")
				.orElseThrow(() -> new IllegalStateException("발급 가능한 쿠폰이 없습니다.")) :
			couponRepository.findFirstByPromotionAndStatus(promotion, "AVAILABLE")
				.orElseThrow(() -> new IllegalStateException("발급 가능한 쿠폰이 없습니다."));
	}

	@Transactional
	public CouponUpdateResponseDto updateCoupon(UUID couponId, CouponUpdateRequestDto requestDto) {
		// 쿠폰 조회
		Coupon coupon = couponRepository.findById(couponId)
			.orElseThrow(() -> new IllegalArgumentException("유효하지 않은 쿠폰 ID입니다."));

		// 상태 업데이트
		Coupon updatedCoupon = coupon.toBuilder()
			.status(requestDto.status())
			.build();
		couponRepository.save(updatedCoupon);

		// 응답 생성
		return new CouponUpdateResponseDto(
			"SUCCESS",
			updatedCoupon.getCouponId().toString(),
			"쿠폰 상태가 성공적으로 수정되었습니다."
		);
	}

	@Transactional
	public CouponUpdateResponseDto deleteCoupon(UUID couponId) {
		// 쿠폰 조회
		Coupon coupon = couponRepository.findById(couponId)
			.orElseThrow(() -> new IllegalArgumentException("유효하지 않은 쿠폰 ID입니다."));

		// Soft Delete 처리
		Coupon deletedCoupon = coupon.toBuilder()
			.status("DELETED")
			.build();
		couponRepository.save(deletedCoupon);

		// 응답 생성
		return new CouponUpdateResponseDto(
			"SUCCESS",
			deletedCoupon.getCouponId().toString(),
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
	public CouponUseResponseDto useCoupon(UUID couponId, UUID programId) {
		// 1. 쿠폰 조회
		Coupon coupon = couponRepository.findById(couponId)
			.orElseThrow(() -> new IllegalArgumentException("유효하지 않은 쿠폰 ID입니다."));

		// 2. 쿠폰 상태 확인
		if (!coupon.getStatus().equals("ISSUED")) {
			String message = switch (coupon.getStatus()) {
				case "AVAILABLE" -> "쿠폰이 발급되지 않았습니다.";
				case "EXPIRED" -> "쿠폰이 만료되었습니다.";
				case "USED" -> "쿠폰이 이미 사용되었습니다.";
				default -> "알 수 없는 쿠폰 상태입니다.";
			};
			return new CouponUseResponseDto("FAILURE", message);
		}

		// 3. 쿠폰 상태를 "USED"로 업데이트
		coupon = coupon.toBuilder()
			.status("USED")
			.build();
		couponRepository.save(coupon);

		// 4. 응답 생성
		return new CouponUseResponseDto("SUCCESS", "쿠폰이 사용되었습니다.");
	}

	@Transactional
	public CouponTransferResponseDto transferCoupon(UUID couponId, UUID toUserId) {
		// 1. `UserCoupon`에서 쿠폰 조회
		UserCoupon userCoupon = userCouponRepository.findByCouponId(couponId)
			.orElseThrow(() -> new IllegalArgumentException("유효하지 않은 쿠폰 ID입니다."));

		// 2. 쿠폰 상태 확인
		if (!"AVAILABLE".equals(userCoupon.getStatus())) {
			return new CouponTransferResponseDto(
				"FAILURE",
				"사용 중이거나 양도할 수 없는 쿠폰입니다."
			);
		}

		// 3. 쿠폰 양도 처리
		userCoupon = userCoupon.toBuilder()
			.userId(toUserId) // 새로운 사용자 ID로 변경
			.fromUserId(userCoupon.getUserId()) // 기존 사용자 ID 기록
			.transferDate(LocalDateTime.now()) // 양도 날짜 기록
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

		// Repository에서 사용자 ID, 프로모션 ID 및 상태로 필터링
		return userCouponRepository.findByUserAndPromotionAndStatus(userId, promotionId, status, pageable);
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

}