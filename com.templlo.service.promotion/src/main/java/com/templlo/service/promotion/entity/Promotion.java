package com.templlo.service.promotion.entity;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.templlo.service.common.config.BaseEntity;
import com.templlo.service.coupon.entity.Coupon;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "promotion")
public class Promotion extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID) // UUID 전략 사용
	@Column(name = "promotion_id", nullable = false, updatable = false)
	private UUID promotionId;

	@Column(name = "name", nullable = false, length = 100)
	private String name;

	@Column(name = "type", nullable = false, length = 50)
	private String type; // 예: HOTDEAL, DISCOUNT 등

	@Column(name = "start_date", nullable = false)
	private LocalDate startDate;

	@Column(name = "end_date", nullable = false)
	private LocalDate endDate;

	@Column(name = "status", nullable = false, length = 20)
	private String status; // ACTIVE, INACTIVE 등

	@Column(name = "coupon_type", nullable = false, length = 50)
	private String couponType; // DISCOUNT, GIFT 등

	@Column(name = "male_coupons", nullable = false)
	private int maleCoupons; // 남성용 쿠폰 개수

	@Column(name = "female_coupons", nullable = false)
	private int femaleCoupons; // 여성용 쿠폰 개수

	@Column(name = "total_coupons", nullable = false)
	private int totalCoupons;

	@Column(name = "issued_coupons", nullable = false)
	private int issuedCoupons;

	@Column(name = "remaining_coupons", nullable = false)
	private int remainingCoupons;

	@OneToMany(mappedBy = "promotion", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Coupon> coupons; // 쿠폰 리스트

	@Builder
	public Promotion(UUID promotionId, String name, String type, LocalDate startDate, LocalDate endDate, String status,
		String couponType, int maleCoupons, int femaleCoupons, int totalCoupons,
		int issuedCoupons, int remainingCoupons) {
		this.promotionId = promotionId;
		this.name = name;
		this.type = type;
		this.startDate = startDate;
		this.endDate = endDate;
		this.status = status;
		this.couponType = couponType;
		this.maleCoupons = maleCoupons;
		this.femaleCoupons = femaleCoupons;
		this.totalCoupons = totalCoupons;
		this.issuedCoupons = issuedCoupons;
		this.remainingCoupons = remainingCoupons;
	}

	// 프로모션 속성 업데이트 메서드
	public void updatePromotion(String name, LocalDate startDate, LocalDate endDate,
		Integer maleCoupons, Integer femaleCoupons, Integer totalCoupons,
		String couponType, String status) {
		this.name = name != null ? name : this.name;
		this.startDate = startDate != null ? startDate : this.startDate;
		this.endDate = endDate != null ? endDate : this.endDate;
		this.maleCoupons = maleCoupons != null ? maleCoupons : this.maleCoupons;
		this.femaleCoupons = femaleCoupons != null ? femaleCoupons : this.femaleCoupons;
		this.totalCoupons = totalCoupons != null ? totalCoupons : this.totalCoupons;
		this.couponType = couponType != null ? couponType : this.couponType;
		this.status = status != null ? status : this.status;

		// remainingCoupons는 totalCoupons와 issuedCoupons를 기반으로 계산
		this.remainingCoupons = this.totalCoupons - this.issuedCoupons;
	}

	// 발급된 쿠폰 수 업데이트
	public void updateIssuedCoupons(String gender, int count) {
		if ("MALE".equalsIgnoreCase(gender)) {
			if (maleCoupons >= count) {
				maleCoupons -= count;
				issuedCoupons += count;
				remainingCoupons -= count;
			} else {
				throw new IllegalArgumentException("남성용 쿠폰이 부족합니다.");
			}
		} else if ("FEMALE".equalsIgnoreCase(gender)) {
			if (femaleCoupons >= count) {
				femaleCoupons -= count;
				issuedCoupons += count;
				remainingCoupons -= count;
			} else {
				throw new IllegalArgumentException("여성용 쿠폰이 부족합니다.");
			}
		} else {
			throw new IllegalArgumentException("유효하지 않은 성별입니다.");
		}
	}

	// 상태 변경
	public void updateStatus(String newStatus) {
		this.status = newStatus;
	}

	// Reflection을 사용하여 BaseEntity의 createdBy 설정
	public void setCreatedBy(String createdBy) {
		setPrivateField("createdBy", createdBy);
	}

	// Reflection을 사용하여 BaseEntity의 updatedBy 설정
	public void setUpdatedBy(String updatedBy) {
		setPrivateField("updatedBy", updatedBy);
	}

	private void setPrivateField(String fieldName, String value) {
		try {
			Field field = BaseEntity.class.getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(this, value);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException("Failed to set field " + fieldName, e);
		}
	}
}
