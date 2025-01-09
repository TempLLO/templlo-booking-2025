package com.templlo.service.user.external.kafka.producer;

import com.templlo.service.user.external.kafka.producer.dto.ReviewRewardEventDto;

public interface UserEventProducer {
	void publishReviewRewardCoupon(ReviewRewardEventDto eventDto); // TODO 쿠폰 발행에 실패한 경우 보상 어떻게 하는지 -> 실패를 어딘가 기록해뒀다가 스케줄러 처리 ?
}
