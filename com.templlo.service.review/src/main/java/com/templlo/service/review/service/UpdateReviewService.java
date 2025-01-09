package com.templlo.service.review.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.templlo.service.review.common.exception.baseException.ReviewNotFoundException;
import com.templlo.service.review.dto.UpdateReviewRequestDto;
import com.templlo.service.review.entity.Review;
import com.templlo.service.review.external.kafka.producer.ReviewEventProducer;
import com.templlo.service.review.external.kafka.producer.dto.ReviewUpdatedEventDto;
import com.templlo.service.review.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UpdateReviewService {

	private final ReviewRepository reviewRepository;
	private final ReviewEventProducer reviewEventProducer;

	@Transactional
	public void updateReview(UUID reviewId, UpdateReviewRequestDto request) {
		Review findReview = reviewRepository.findById(reviewId).orElseThrow(ReviewNotFoundException::new);
		Double oldRating = findReview.getRating();

		findReview.updateReview(request.rating(), request.content());

		// TODO 트랜잭션이 순차적으로 진행되는건지 확인이 필요 #2
		ReviewUpdatedEventDto eventDto = ReviewUpdatedEventDto.of(findReview.getProgramId(), oldRating, findReview.getRating());
		reviewEventProducer.publishReviewUpdated(eventDto);
	}
}