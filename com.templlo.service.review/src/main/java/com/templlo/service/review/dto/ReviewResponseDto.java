package com.templlo.service.review.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.domain.Page;

import com.templlo.service.review.common.response.PageResponse;
import com.templlo.service.review.entity.Review;

public record ReviewResponseDto(
	UUID reviewId,
	UUID userId,
	UUID programId,
	Double rating,
	String content,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {

	public static ReviewResponseDto from(Review review) {
		return new ReviewResponseDto(review.getId(), review.getUserId(), review.getProgramId(), review.getRating(),
			review.getContent(), review.getCreatedAt(), review.getUpdatedAt());
	}

	public static PageResponse<ReviewResponseDto> pageOf(Page<Review> pagingData) {
		Page<ReviewResponseDto> responses = pagingData.map(ReviewResponseDto::from);
		return PageResponse.of(responses);
	}
}
