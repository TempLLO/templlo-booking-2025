package com.templlo.service.review.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "review")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Review {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false)
	private UUID programId;

	@Column(nullable = false)
	private UUID userId;

	@Column(columnDefinition = "text")
	private String content;

	@Column(nullable = false, columnDefinition = "DECIMAL(2,1)") // 전체 2자리, 소수점 1자리
	private Double rating;

	@CreatedBy
	@Column(updatable = false)
	private String createdBy;

	@CreatedDate
	@Column(updatable = false)
	private LocalDateTime createdAt;

	@LastModifiedBy
	private String updateBy;

	@LastModifiedDate
	private LocalDateTime updatedAt;

	private boolean isDeleted;

	private String deletedBy;

	private LocalDateTime deletedAt;

	@Builder(access = AccessLevel.PRIVATE)
	public Review(UUID programId, UUID userId, Double rating, String content) {
		this.programId = programId;
		this.userId = userId;
		this.rating = rating;
		this.content = content;
		this.isDeleted = false;
	}

	public static Review create(UUID programId, UUID loginId, Double rating, String content) {
		return new Review(programId, loginId, rating, content);
	}

	public void updateReview(Double rating, String content) {
		this.rating = rating;
		this.content = content;
	}
}
