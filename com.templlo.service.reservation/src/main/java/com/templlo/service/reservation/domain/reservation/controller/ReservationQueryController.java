package com.templlo.service.reservation.domain.reservation.controller;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.templlo.service.reservation.domain.reservation.controller.exception.ReservationStatusCode;
import com.templlo.service.reservation.domain.reservation.controller.model.response.ReservationDetailRes;
import com.templlo.service.reservation.domain.reservation.controller.model.response.TempleReservationListWrapperRes;
import com.templlo.service.reservation.domain.reservation.controller.model.response.UserReservationListWrapperRes;
import com.templlo.service.reservation.domain.reservation.service.ReservationQueryService;
import com.templlo.service.reservation.global.PageUtil;
import com.templlo.service.reservation.global.common.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReservationQueryController {
	private final ReservationQueryService reservationQueryService;

	@GetMapping("/reservations/{reservationId}")
	public ResponseEntity<ApiResponse<ReservationDetailRes>> getReservationDetail(
		@PathVariable(name = "reservationId") UUID reservationId
	) {
		ReservationDetailRes responseDto = reservationQueryService.getReservationById(reservationId);
		return ResponseEntity.ok().body(
			ApiResponse.of(ReservationStatusCode.GET_RESERVATION_SUCCESS, responseDto));
	}

	@GetMapping("/reservations/users/{userId}/reservations")
	public ResponseEntity<ApiResponse<UserReservationListWrapperRes>> getReservationsByUser(
		@PathVariable(name = "userId") UUID userId,
		Pageable pageable
	) {
		Pageable pageRequest = PageUtil.getCheckedPageable(pageable);
		UserReservationListWrapperRes responseDto = reservationQueryService.getReservationsByUser(userId, pageRequest);
		return ResponseEntity.ok().body(
			ApiResponse.of(ReservationStatusCode.GET_RESERVATIONS_OF_USER_SUCCESS, responseDto));
	}

	@GetMapping("/reservations/temples/{templeId}/reservations")
	public ResponseEntity<ApiResponse<TempleReservationListWrapperRes>> getReservationsByTemple(
		@PathVariable(name = "templeId") UUID templeId,
		Pageable pageable,
		@RequestParam(name = "tempProgramId1") UUID tempProgramId1,
		@RequestParam(name = "tempProgramId2") UUID tempProgramId2
	) {
		Pageable pageRequest = PageUtil.getCheckedPageable(pageable);
		TempleReservationListWrapperRes responseDto = reservationQueryService.getReservationsByTemple(templeId,
			pageRequest, tempProgramId1, tempProgramId2);
		return ResponseEntity.ok().body(
			ApiResponse.of(ReservationStatusCode.GET_RESERVATIONS_OF_TEMPLE_SUCCESS, responseDto));
	}
}
