package com.templlo.service.review.external.kafka.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.templlo.service.review.entity.Review;

public record ReviewCreatedEventDto(
	@JsonProperty
	UUID userId,

	@JsonProperty
	UUID programId,

	@JsonProperty
	Double rating

) {

	public static ReviewCreatedEventDto from(Review review) {
		return new ReviewCreatedEventDto(review.getUserId(), review.getProgramId(), review.getRating());
	}
}
