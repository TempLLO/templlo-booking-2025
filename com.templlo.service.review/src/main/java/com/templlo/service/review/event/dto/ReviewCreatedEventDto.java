package com.templlo.service.review.event.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.templlo.service.review.entity.Review;
import com.templlo.service.review.event.external.topic.ProducerTopic;

public record ReviewCreatedEventDto(
	@JsonProperty
	String loginId,

	@JsonProperty
	UUID reviewId,

	@JsonProperty
	UUID programId,

	@JsonProperty
	Double rating

) implements OutboxEvent{

	public static ReviewCreatedEventDto of(String loginId, Review review) {
		return new ReviewCreatedEventDto(loginId, review.getId(), review.getProgramId(), review.getRating());
	}

	@Override
	public String getTopic() {
		return ProducerTopic.REVIEW_CREATED.toString();
	}

	@Override
	public UUID getReviewId() {
		return reviewId;
	}
}
