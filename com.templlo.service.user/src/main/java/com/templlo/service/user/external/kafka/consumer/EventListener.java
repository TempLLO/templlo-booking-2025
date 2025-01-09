package com.templlo.service.user.external.kafka.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.templlo.service.user.external.kafka.consumer.dto.ReviewCreatedEventDto;
import com.templlo.service.user.external.kafka.topic.ConsumerTopic;
import com.templlo.service.user.service.UpdateUserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = " Event Listener ")
@Component
@RequiredArgsConstructor
public class EventListener {

	private final ObjectMapper objectMapper;
	private final UpdateUserService updateUserService;

	@KafkaListener(topics = ConsumerTopic.REVIEW_CREATED, groupId = "user-consumer-group-review", containerFactory = "listenerFactory")
	public void handleReviewCreated(String message) {
		log.info("Consumed Message From Topic : {} ", ConsumerTopic.REVIEW_CREATED);
		try {
			// Todo serializer class 따로 구현할지
			ReviewCreatedEventDto eventDto = objectMapper.readValue(message, ReviewCreatedEventDto.class);
			updateUserService.updateReviewCount(eventDto);
		} catch (JsonProcessingException e) {
			log.error("JSON Parsing Error : {}", e.getMessage());
		}
	}

}
