package com.templlo.service.review.external.kafka.producer;

import static com.templlo.service.review.external.kafka.topic.ProducerTopic.*;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.templlo.service.review.external.kafka.producer.dto.ReviewCreatedEventDto;
import com.templlo.service.review.external.kafka.producer.dto.ReviewUpdatedEventDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j(topic = "Review Event Producer ")
@RequiredArgsConstructor
public class ReviewEventProducerImpl implements ReviewEventProducer {

	private final KafkaTemplate<String, Object> kafkaTemplate;
	private final String EVENT_LOG = " *** [Topic] %s, [Message] %s";

	@Override
	public void publishReviewCreated(ReviewCreatedEventDto eventDto) {
		kafkaTemplate.send(REVIEW_CREATED.toString(), eventDto);
		log.info(String.format(EVENT_LOG, REVIEW_CREATED, eventDto));
	}

	@Override
	public void publishReviewUpdated(ReviewUpdatedEventDto eventDto) {
		kafkaTemplate.send(REVIEW_UPDATED.toString(), eventDto);
		log.info(String.format(EVENT_LOG, REVIEW_UPDATED, eventDto));
	}
}
