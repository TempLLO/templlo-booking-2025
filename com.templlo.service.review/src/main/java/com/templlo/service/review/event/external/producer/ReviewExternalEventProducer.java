package com.templlo.service.review.event.external.producer;

import com.templlo.service.review.event.dto.ReviewCreatedEventDto;
import com.templlo.service.review.event.dto.ReviewUpdatedEventDto;

public interface ReviewExternalEventProducer {

	void publishReviewCreated(ReviewCreatedEventDto eventDto);

	void publishReviewUpdated(ReviewUpdatedEventDto eventDto);

}
