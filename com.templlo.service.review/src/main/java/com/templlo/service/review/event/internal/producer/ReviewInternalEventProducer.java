package com.templlo.service.review.event.internal.producer;

import com.templlo.service.review.entity.ReviewOutbox;

public interface ReviewInternalEventProducer {

	void publishReviewCreated(ReviewOutbox outbox);
}
