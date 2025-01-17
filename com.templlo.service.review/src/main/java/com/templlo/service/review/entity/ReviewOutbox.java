package com.templlo.service.review.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "review_outbox")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewOutbox {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String eventType;

	@Column(nullable = false)
	private UUID reviewId;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private EventStatus status;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String payload;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	private LocalDateTime updatedAt;

	@Builder(access = AccessLevel.PRIVATE)
	public ReviewOutbox(String eventType, UUID reviewId, String payload) {
		this.eventType = eventType;
		this.reviewId = reviewId;
		this.status = EventStatus.INIT;
		this.payload = payload;
		this.createdAt = LocalDateTime.now();
	}

	public static ReviewOutbox create(String eventType, UUID reviewId, String payload){
		return new ReviewOutbox(eventType, reviewId, payload);
	}

	public void markAsPublished() {
		this.status = EventStatus.PUBLISHED;
		this.updatedAt = LocalDateTime.now();
	}

	public void markAsFailed() {
		this.status = EventStatus.FAILED;
		this.updatedAt = LocalDateTime.now();
	}
}
