package com.templlo.service.review.external.kafka.producer;

import com.templlo.service.review.external.kafka.dto.ReviewCreatedEventDto;

public interface ReviewEventProducer {
	void publishReviewCreated(ReviewCreatedEventDto eventDto);
}
