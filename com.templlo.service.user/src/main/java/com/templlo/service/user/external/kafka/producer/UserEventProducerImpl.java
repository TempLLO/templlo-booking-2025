package com.templlo.service.user.external.kafka.producer;

import static com.templlo.service.user.external.kafka.topic.ProducerTopic.*;

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

	@Override
	public void publishReviewRewardCoupon(ReviewRewardEventDto eventDto) {
		kafkaTemplate.send(REVIEW_REWARD_COUPON.toString(), eventDto);
		log.info("Topic : {} , Publishing Event Msg = {} ", REVIEW_REWARD_COUPON, eventDto);
	}
}
