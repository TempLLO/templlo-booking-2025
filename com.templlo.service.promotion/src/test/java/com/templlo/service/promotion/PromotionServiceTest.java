package com.templlo.service.promotion;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.support.Acknowledgment;

import com.templlo.service.coupon.entity.Coupon;
import com.templlo.service.coupon.repository.CouponRepository;
import com.templlo.service.kafka.KafkaConsumerService;
import com.templlo.service.promotion.dto.PromotionCreatedEvent;
import com.templlo.service.promotion.entity.Promotion;
import com.templlo.service.promotion.repository.PromotionRepository;

class PromotionServiceTest {

	@InjectMocks
	private KafkaConsumerService kafkaConsumerService; // 테스트 대상 클래스

	@Mock
	private PromotionRepository promotionRepository;

	@Mock
	private CouponRepository couponRepository;

	@Mock
	private Acknowledgment acknowledgment;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void testHandlePromotionCreated_Success() {
		// Given
		PromotionCreatedEvent event = new PromotionCreatedEvent(
			UUID.fromString("123e4567-e89b-12d3-a456-426614174000"), // UUID
			"Spring Sale",
			LocalDate.now(),
			LocalDate.now().plusDays(7),
			"DISCOUNT",
			50 // 총 쿠폰 수량
		);

		Promotion promotion = Promotion.builder()
			.promotionId(event.promotionId()) // 추가
			.name("Spring Sale")
			.startDate(LocalDate.now())
			.endDate(LocalDate.now().plusDays(7))
			.build();

		when(promotionRepository.findById(event.promotionId())).thenReturn(Optional.of(promotion));
		when(couponRepository.existsByPromotion_PromotionId(event.promotionId())).thenReturn(false);

		// When
		kafkaConsumerService.handlePromotionCreated(event, acknowledgment);

		// Then
		verify(couponRepository, times(50)).save(any(Coupon.class)); // 쿠폰 50개 저장 확인
		verify(acknowledgment, times(1)).acknowledge(); // Kafka 메시지 ACK 확인
	}

	@Test
	void testHandlePromotionCreated_PromotionNotFound() {
		// Given
		PromotionCreatedEvent event = new PromotionCreatedEvent(
			UUID.fromString("123e4567-e89b-12d3-a456-426614174001"), // UUID
			"Summer Sale",
			LocalDate.now(),
			LocalDate.now().plusDays(10),
			"GIFT",
			100
		);

		// Mock 설정: 프로모션 ID로 조회 시 Optional.empty 반환
		when(promotionRepository.findById(event.promotionId())).thenReturn(Optional.empty());

		// When & Then
		IllegalArgumentException exception = assertThrows(
			IllegalArgumentException.class,
			() -> kafkaConsumerService.handlePromotionCreated(event, acknowledgment)
		);

		// 검증: 예외 메시지와 호출 여부
		assertEquals("프로모션을 찾을 수 없습니다.", exception.getMessage());
		verify(promotionRepository, times(1)).findById(event.promotionId());
		verify(acknowledgment, never()).acknowledge(); // ACK 호출이 없어야 함
	}

	@Test
	void testHandlePromotionCreated_AlreadyProcessed() {
		// Given
		PromotionCreatedEvent event = new PromotionCreatedEvent(
			UUID.fromString("123e4567-e89b-12d3-a456-426614174002"), // UUID
			"Winter Sale",
			LocalDate.now(),
			LocalDate.now().plusDays(15),
			"DISCOUNT",
			30
		);

		Promotion promotion = Promotion.builder()
			.name("Winter Sale")
			.build();

		when(promotionRepository.findById(event.promotionId())).thenReturn(Optional.of(promotion));
		when(couponRepository.existsByPromotion_PromotionId(event.promotionId())).thenReturn(true);

		// When
		kafkaConsumerService.handlePromotionCreated(event, acknowledgment);

		// Then
		verify(couponRepository, never()).save(any(Coupon.class)); // 쿠폰 저장 호출이 없어야 함
		verify(acknowledgment, times(1)).acknowledge(); // Kafka 메시지 ACK 확인
	}
}
