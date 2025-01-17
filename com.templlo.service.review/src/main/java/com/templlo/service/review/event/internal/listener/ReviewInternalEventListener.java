package com.templlo.service.review.event.internal.listener;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.templlo.service.review.entity.ReviewOutbox;
import com.templlo.service.review.service.ReviewOutboxService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "Review Internal Event Listener")
@Component
@RequiredArgsConstructor
public class ReviewInternalEventListener {

	private final ReviewOutboxService outBoxService;

	@TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
	public void handleReviewCreatedEvent(ReviewOutbox outbox) {
		outBoxService.saveEvent(outbox);
	}

}
