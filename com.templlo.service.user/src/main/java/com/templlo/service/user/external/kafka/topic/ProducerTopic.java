package com.templlo.service.user.external.kafka.topic;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProducerTopic {

	REVIEW_REWARD_COUPON("review-reward-coupon", "유저 리뷰작성횟수가 쿠폰 발급 기준 충족시 발행하는 이벤트입니다.");

	private final String topic;
	private final String description;

	@Override
	public String toString() {
		return this.topic;
	}

}