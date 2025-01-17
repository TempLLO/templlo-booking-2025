package com.templlo.service.review.event.topic;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProducerTopic {

	REVIEW_CREATED("review-created", "리뷰 작성시 발행하는 이벤트입니다."),
	REVIEW_UPDATED("review-updated", "리뷰 수정시 발행하는 이벤트입니다.");

	private final String topic;
	private final String description;

	@Override
	public String toString() {
		return this.topic;
	}

}