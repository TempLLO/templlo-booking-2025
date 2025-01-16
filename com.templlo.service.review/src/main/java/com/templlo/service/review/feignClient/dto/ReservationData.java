package com.templlo.service.review.feignClient.dto;

import java.util.UUID;

public record ReservationData(
	UUID reservationId,
	UUID programId,
	String programDate,
	UUID userId,
	ReservationStatus status,
	String name,
	String phoneNumber
) {
}
