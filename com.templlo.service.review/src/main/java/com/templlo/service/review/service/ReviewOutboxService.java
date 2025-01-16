package com.templlo.service.review.service;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.templlo.service.review.common.exception.baseException.NotFoundException;
import com.templlo.service.review.entity.ReviewOutbox;
import com.templlo.service.review.event.dto.OutboxEvent;
import com.templlo.service.review.repository.ReviewOutboxRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ReviewOutboxService {

	private static final Logger log = LoggerFactory.getLogger(ReviewOutboxService.class);
	private final ReviewOutboxRepository outBoxRepository;
	private final ObjectMapper objectMapper;

	public void saveEvent(OutboxEvent event) {
		try {
			String jsonData = objectMapper.writeValueAsString(event);
			ReviewOutbox outbox = ReviewOutbox.create(event.getTopic(), event.getReviewId(), jsonData);
			outBoxRepository.save(outbox);
			log.info("step4: outbox에 저장 완료");
		} catch (Exception e) {
			log.info("outbox service json parsing error!!!!!!!!");
		}

	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void updateStatus(UUID reviewId, boolean success) {
		log.info("step6: mark 접속 완료 ");
		ReviewOutbox outbox = outBoxRepository.findByReviewId(reviewId)
			.orElseThrow(NotFoundException::new);

		log.info("Current status before update: {}", outbox.getStatus());

		if (success) {
			outbox.markAsPublished();
			log.info("카프카까지 발행 성공 응답 수신 완료 !!! ");
		} else {
			outbox.markAsFailed();
			log.info("실패 기록 완료 !!!!!!");
		}

		log.info("New status after update: {}", outbox.getStatus());

		// 상태 값 직접 출력
		log.info("Status name: {}", outbox.getStatus().name());
	}

}
