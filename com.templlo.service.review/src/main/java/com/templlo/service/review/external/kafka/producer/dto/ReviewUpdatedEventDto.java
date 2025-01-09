package com.templlo.service.review.external.kafka.producer.dto;

import java.util.UUID;

public record ReviewUpdatedEventDto(
	UUID programId,
	Double oldRating,
	Double newRating
) {

	public static ReviewUpdatedEventDto of(UUID programId, Double oldRating, Double newRating) {
		return new ReviewUpdatedEventDto(programId, oldRating, newRating);
	}
}
