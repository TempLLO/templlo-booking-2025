package com.templlo.service.user.external.kafka.producer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.templlo.service.user.external.kafka.producer.dto.ReviewRewardEventDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j(topic = "User Event Producer")
@RequiredArgsConstructor
public class UserEventProducerImpl implements UserEventProducer {

	private final KafkaTemplate<String, Object> kafkaTemplate;

	@Value("${spring.kafka.topics.review-reward-coupon}")
	private String topicReviewReward;

	@Override
	public void publishReviewRewardCoupon(ReviewRewardEventDto eventDto) {
		kafkaTemplate.send(topicReviewReward, eventDto);
		log.info("Topic : {} , Publishing Event Msg = {} ", topicReviewReward, eventDto);
	}
}
