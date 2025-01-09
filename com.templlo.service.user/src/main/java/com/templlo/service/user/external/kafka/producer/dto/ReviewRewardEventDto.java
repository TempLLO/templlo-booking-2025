package com.templlo.service.user.external.kafka.producer.dto;

import java.util.UUID;

public record ReviewRewardEventDto(
	UUID userId
) {
}
