package com.templlo.service.review.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.templlo.service.review.common.exception.baseException.NotFoundException;
import com.templlo.service.review.entity.ReviewOutbox;
import com.templlo.service.review.repository.ReviewOutboxRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "Outbox Service")
@RequiredArgsConstructor
@Service
public class ReviewOutboxService {

	private final ReviewOutboxRepository outBoxRepository;
	private final ObjectMapper objectMapper;

	public void saveEvent(ReviewOutbox outbox) {
		outBoxRepository.save(outbox);
		log.info("Outbox saved successfully");
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void updateStatus(UUID reviewId, boolean success) {
		ReviewOutbox outbox = outBoxRepository.findByReviewId(reviewId)
			.orElseThrow(NotFoundException::new);

		log.info("Current status before update: {}", outbox.getStatus());

		if (success) {
			outbox.markAsPublished();
		} else {
			outbox.markAsFailed();
		}

		log.info("Current status after update: {}", outbox.getStatus());
	}

}
