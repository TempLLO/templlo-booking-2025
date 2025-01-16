package com.templlo.service.review.event.internal.producer;

import com.templlo.service.review.event.dto.ReviewCreatedEventDto;

public interface ReviewInternalEventProducer {

	void publishReviewCreated(ReviewCreatedEventDto eventDto);
}
