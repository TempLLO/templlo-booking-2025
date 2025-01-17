package com.templlo.service.review.event.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.templlo.service.review.entity.Review;

public record ReviewCreatedEventDto(
	@JsonProperty
	String loginId,

	@JsonProperty
	UUID programId,

	@JsonProperty
	Double rating

) {

	public static ReviewCreatedEventDto of(String loginId, Review review) {
		return new ReviewCreatedEventDto(loginId, review.getProgramId(), review.getRating());
	}

}
