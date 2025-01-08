package com.templlo.service.review.external.feignClient.client;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.templlo.service.review.common.response.ApiResponse;
import com.templlo.service.review.external.feignClient.config.FeignConfiguration;
import com.templlo.service.review.external.feignClient.dto.ReservationData;

@FeignClient(
	name = "reservation-service",
	configuration = FeignConfiguration.class)
public interface ReservationClient {

	@GetMapping("/api/reservations")
	ApiResponse<ReservationData> getReservationInfo(@RequestParam(name = "userId") UUID userId);
}
