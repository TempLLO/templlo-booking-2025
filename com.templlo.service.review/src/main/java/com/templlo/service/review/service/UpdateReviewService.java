package com.templlo.service.review.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.templlo.service.review.common.exception.baseException.ReviewNotFoundException;
import com.templlo.service.review.dto.UpdateReviewRequestDto;
import com.templlo.service.review.entity.Review;
import com.templlo.service.review.event.external.producer.ReviewExternalEventProducer;
import com.templlo.service.review.event.dto.ReviewUpdatedEventDto;
import com.templlo.service.review.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UpdateReviewService {

	private final ReviewRepository reviewRepository;
	private final ReviewExternalEventProducer reviewEventProducer;

	@Transactional
	public void updateReview(UUID reviewId, UpdateReviewRequestDto request) {
		Review findReview = reviewRepository.findById(reviewId).orElseThrow(ReviewNotFoundException::new);
		Double oldRating = findReview.getRating();

		findReview.updateReview(request.rating(), request.content());

		ReviewUpdatedEventDto eventDto = ReviewUpdatedEventDto.of(findReview.getProgramId(), oldRating, findReview.getRating());
		reviewEventProducer.publishReviewUpdated(eventDto);
	}
}