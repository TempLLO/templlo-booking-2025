package com.templlo.service.promotion.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "promotion")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Promotion {

	@Id
	@GeneratedValue
	private UUID promotionId; // 프로모션 ID

	@Column(nullable = false)
	private String name; // 프로모션 이름

	@Column(nullable = false)
	private String type; // 프로모션 유형 (예: HOTDEAL, MEMBERSHIP 등)

	@Column(nullable = false)
	private LocalDate startDate; // 시작일

	@Column(nullable = false)
	private LocalDate endDate; // 종료일

	@Column(nullable = false)
	private String couponType; // 쿠폰 유형 (예: 할인, 입장권 등)

	@Column(nullable = true) // null 허용
	private Integer maleCoupons; // 남성 쿠폰 수량

	@Column(nullable = true) // null 허용
	private Integer femaleCoupons; // 여성 쿠폰 수량

	@Column(nullable = false, columnDefinition = "integer default 0")
	private Integer totalCoupons; // 전체 쿠폰 수량

	@Column(nullable = false, columnDefinition = "integer default 0")
	private Integer issuedCoupons; // 발급된 쿠폰 수량

	@Column(nullable = false, columnDefinition = "integer default 0")
	private Integer remainingCoupons; // 남은 쿠폰 수량

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt; // 생성일자

	@Column(nullable = false)
	private LocalDateTime updatedAt; // 수정일자

	@Column(nullable = false, columnDefinition = "varchar(50) default 'ACTIVE'")
	private String status; // 상태

	@PrePersist
	public void onCreate() {
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
		if (this.status == null) {
			this.status = "ACTIVE"; // 기본값 설정
		}
	}

	@PreUpdate
	public void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}

	public Promotion updatePromotion(String name, LocalDate startDate, LocalDate endDate,
		Integer maleCoupons, Integer femaleCoupons, Integer totalCoupons,
		String couponType, String status) {
		if (name != null) {
			this.name = name;
		}
		if (startDate != null) {
			this.startDate = startDate;
		}
		if (endDate != null) {
			this.endDate = endDate;
		}
		if (maleCoupons != null) {
			this.remainingCoupons =
				this.remainingCoupons - (this.maleCoupons != null ? this.maleCoupons : 0) + maleCoupons;
			this.maleCoupons = maleCoupons;
		}
		if (femaleCoupons != null) {
			this.remainingCoupons =
				this.remainingCoupons - (this.femaleCoupons != null ? this.femaleCoupons : 0) + femaleCoupons;
			this.femaleCoupons = femaleCoupons;
		}
		if (totalCoupons != null) {
			this.totalCoupons = totalCoupons;
		}
		if (couponType != null) {
			this.couponType = couponType;
		}
		if (status != null) {
			this.status = status;
		}
		return this;
	}
}
