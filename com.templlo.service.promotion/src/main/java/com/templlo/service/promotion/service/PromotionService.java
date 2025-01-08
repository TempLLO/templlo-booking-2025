package com.templlo.service.promotion.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.templlo.service.coupon.entity.Coupon;
import com.templlo.service.coupon.repository.CouponRepository;
import com.templlo.service.promotion.dto.PromotionDetailResponseDto;
import com.templlo.service.promotion.dto.PromotionRequestDto;
import com.templlo.service.promotion.dto.PromotionResponseDto;
import com.templlo.service.promotion.dto.PromotionUpdateDto;
import com.templlo.service.promotion.entity.Promotion;
import com.templlo.service.promotion.repository.PromotionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PromotionService {

	private final PromotionRepository promotionRepository;
	private final CouponRepository couponRepository;

	@Transactional
	public PromotionResponseDto createPromotion(PromotionRequestDto requestDto, String userId, String role) {
		// 1. 역할 검증
		if (!"MASTER".equalsIgnoreCase(role)) {
			throw new IllegalArgumentException("권한이 부족합니다. 프로모션을 생성하려면 ADMIN 역할이 필요합니다.");
		}

		// 2. 프로모션 저장
		Promotion promotion = Promotion.builder()
			.name(requestDto.name())
			.type(requestDto.type())
			.startDate(requestDto.startDate())
			.endDate(requestDto.endDate())
			.couponType(requestDto.couponType())
			.maleCoupons(requestDto.maleCoupon() != null ? requestDto.maleCoupon() : 0)
			.femaleCoupons(requestDto.femaleCoupon() != null ? requestDto.femaleCoupon() : 0)
			.totalCoupons(requestDto.totalCoupon())
			.issuedCoupons(0) // 발급된 쿠폰 초기화
			.remainingCoupons(requestDto.totalCoupon()) // 남은 쿠폰 초기화
			.status(requestDto.status() != null ? requestDto.status() : "ACTIVE")
			.createdBy(userId) // 생성자 정보 저장
			.build();

		promotion = promotionRepository.save(promotion);

		// 3. 쿠폰 생성 로직
		int maleCouponCount = requestDto.maleCoupon() != null ? requestDto.maleCoupon() : 0;
		int femaleCouponCount = requestDto.femaleCoupon() != null ? requestDto.femaleCoupon() : 0;
		int remainingCoupons = requestDto.totalCoupon() - maleCouponCount - femaleCouponCount;

		// 남성용 쿠폰 생성
		for (int i = 0; i < maleCouponCount; i++) {
			Coupon maleCoupon = Coupon.builder()
				.promotion(promotion)
				.gender("MALE")
				.status("AVAILABLE")
				.build();
			couponRepository.save(maleCoupon);
		}

		// 여성용 쿠폰 생성
		for (int i = 0; i < femaleCouponCount; i++) {
			Coupon femaleCoupon = Coupon.builder()
				.promotion(promotion)
				.gender("FEMALE")
				.status("AVAILABLE")
				.build();
			couponRepository.save(femaleCoupon);
		}

		// 성별 구분 없는 쿠폰 생성
		for (int i = 0; i < remainingCoupons; i++) {
			Coupon generalCoupon = Coupon.builder()
				.promotion(promotion)
				.gender(null) // 성별 없음
				.status("AVAILABLE")
				.build();
			couponRepository.save(generalCoupon);
		}

		return new PromotionResponseDto(
			promotion.getPromotionId(),
			"SUCCESS",
			"프로모션이 생성되었습니다."
		);
	}

	@Transactional
	public PromotionResponseDto updatePromotion(UUID promotionId, PromotionUpdateDto updateDto) {
		// 1. 기존 프로모션 조회
		Promotion promotion = promotionRepository.findById(promotionId)
			.orElseThrow(() -> new IllegalArgumentException("프로모션을 찾을 수 없습니다."));

		// 2. 요청된 데이터로 프로모션 업데이트
		promotion.updatePromotion(
			updateDto.name(),
			updateDto.startDate(),
			updateDto.endDate(),
			updateDto.maleCoupons(),
			updateDto.femaleCoupons(),
			updateDto.totalCoupons(),
			updateDto.couponType(),
			updateDto.status()
		);

		// 3. 데이터베이스에 저장
		promotionRepository.save(promotion);

		// 4. 성공 메시지 반환
		return new PromotionResponseDto(
			promotion.getPromotionId(),
			"SUCCESS",
			"프로모션이 수정되었습니다."
		);
	}

	public PromotionResponseDto deletePromotion(UUID promotionId) {
		// 1. 프로모션 조회
		Promotion promotion = promotionRepository.findById(promotionId)
			.orElseThrow(() -> new IllegalArgumentException("프로모션을 찾을 수 없습니다."));

		// 2. 프로모션 삭제
		promotionRepository.delete(promotion);

		// 3. 성공 메시지 반환
		return new PromotionResponseDto(
			promotionId,
			"SUCCESS",
			"프로모션이 삭제되었습니다."
		);
	}

	public PromotionDetailResponseDto getPromotion(UUID promotionId) {
		// 1. 프로모션 조회
		Promotion promotion = promotionRepository.findById(promotionId)
			.orElseThrow(() -> new IllegalArgumentException("프로모션을 찾을 수 없습니다."));

		// 2. 조회 결과를 DTO로 변환
		return new PromotionDetailResponseDto(
			promotion.getPromotionId(),
			promotion.getName(),
			promotion.getType(),
			promotion.getStartDate(),
			promotion.getEndDate(),
			promotion.getTotalCoupons(),
			promotion.getIssuedCoupons(),
			promotion.getRemainingCoupons(),
			promotion.getMaleCoupons(),
			promotion.getFemaleCoupons(),
			promotion.getStatus()
		);
	}

	public Page<PromotionDetailResponseDto> getPromotions(int page, int size, String type, String status) {
		// 1. 페이지네이션 요청 생성
		PageRequest pageRequest = PageRequest.of(page, size);

		// 2. 조건에 따라 데이터 조회
		Page<Promotion> promotions = promotionRepository.findByFilters(type, status, pageRequest);

		// 3. Promotion -> PromotionDetailResponseDto 매핑
		return promotions.map(promotion -> new PromotionDetailResponseDto(
			promotion.getPromotionId(),
			promotion.getName(),
			promotion.getType(),
			promotion.getStartDate(),
			promotion.getEndDate(),
			promotion.getTotalCoupons(),
			promotion.getIssuedCoupons(),
			promotion.getRemainingCoupons(),
			promotion.getMaleCoupons(),
			promotion.getFemaleCoupons(),
			promotion.getStatus()
		));
	}
}
