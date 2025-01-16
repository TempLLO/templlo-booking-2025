package com.templlo.service.review.event.internal.producer;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.templlo.service.review.event.dto.ReviewCreatedEventDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "Review Internal Event Producer")
@RequiredArgsConstructor
@Component
public class ReviewInternalEventProducerImpl implements ReviewInternalEventProducer {

	private final ApplicationEventPublisher applicationEventPublisher;

	@Override
	public void publishReviewCreated(ReviewCreatedEventDto eventDto) {
		applicationEventPublisher.publishEvent(eventDto);
	}
}