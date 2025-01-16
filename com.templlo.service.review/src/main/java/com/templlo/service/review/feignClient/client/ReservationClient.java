package com.templlo.service.review.feignClient.client;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.templlo.service.review.common.response.ApiResponse;
import com.templlo.service.review.feignClient.config.FeignConfiguration;
import com.templlo.service.review.feignClient.dto.ReservationData;

@FeignClient(
	name = "reservation-service",
	configuration = FeignConfiguration.class)
public interface ReservationClient {

	@GetMapping("/api/reservations/{reservationId}")
	ResponseEntity<ApiResponse<ReservationData>> getReservationInfo(@RequestParam(name = "reservationId") UUID reservationId);
}
