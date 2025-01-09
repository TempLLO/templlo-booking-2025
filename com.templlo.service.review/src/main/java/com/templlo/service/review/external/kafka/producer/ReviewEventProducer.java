package com.templlo.service.review.external.kafka.producer;

import com.templlo.service.review.external.kafka.producer.dto.ReviewCreatedEventDto;
import com.templlo.service.review.external.kafka.producer.dto.ReviewUpdatedEventDto;

public interface ReviewEventProducer {

	void publishReviewCreated(ReviewCreatedEventDto eventDto);

	void publishReviewUpdated(ReviewUpdatedEventDto eventDto);

}
