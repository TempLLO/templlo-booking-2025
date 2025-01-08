package com.templlo.service.review.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.templlo.service.review.common.excepion.baseException.ReviewNotFoundException;
import com.templlo.service.review.dto.UpdateReviewRequestDto;
import com.templlo.service.review.entity.Review;
import com.templlo.service.review.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UpdateReviewService {

	private final ReviewRepository reviewRepository;

	@Transactional
	public void updateReview(UUID reviewId, UpdateReviewRequestDto request) {
		Review findReview = reviewRepository.findById(reviewId)
			.orElseThrow(ReviewNotFoundException::new);

		findReview.updateReview(request.rating(), request.content());

		// TODO 리뷰평점 수정 업데이트 이벤트 발행
	}
}