package com.templlo.service.user.external.kafka.consumer.dto;

import java.util.UUID;

public record ReviewCreatedEventDto(
	String loginId,

	UUID programId,

	Double rating
) {
}
