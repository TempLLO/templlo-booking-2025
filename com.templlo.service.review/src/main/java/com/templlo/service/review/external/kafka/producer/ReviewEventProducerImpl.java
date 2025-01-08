package com.templlo.service.review.external.kafka.producer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.templlo.service.review.external.kafka.dto.ReviewCreatedEventDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j(topic = "=== Review Kafka Producer ====")
@RequiredArgsConstructor
public class ReviewEventProducerImpl implements ReviewEventProducer {

	private final KafkaTemplate<String, Object> kafkaTemplate;

	@Value("${spring.kafka.topics.review-created}")
	private String topicReviewCreated;

	@Override
	public void publishReviewCreated(ReviewCreatedEventDto eventDto) {
		log.info("sending eventDto = {} to topic = {} ", eventDto, topicReviewCreated);
		kafkaTemplate.send(topicReviewCreated, eventDto);
	}
}
