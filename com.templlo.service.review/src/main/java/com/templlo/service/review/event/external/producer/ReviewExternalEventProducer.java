package com.templlo.service.review.event.external.producer;

import com.templlo.service.review.entity.ReviewOutbox;
import com.templlo.service.review.event.dto.ReviewUpdatedEventDto;

public interface ReviewExternalEventProducer {

	void publishReviewCreated(ReviewOutbox outbox);

	void publishReviewUpdated(ReviewUpdatedEventDto eventDto);

}
