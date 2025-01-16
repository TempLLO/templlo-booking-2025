package com.templlo.service.review.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.templlo.service.review.common.exception.baseException.DuplicatedReviewException;
import com.templlo.service.review.common.exception.baseException.NonReviewableException;
import com.templlo.service.review.common.response.ApiResponse;
import com.templlo.service.review.dto.CreateReviewRequestDto;
import com.templlo.service.review.entity.Review;
import com.templlo.service.review.feignClient.client.ReservationClient;
import com.templlo.service.review.feignClient.client.UserClient;
import com.templlo.service.review.feignClient.dto.ReservationData;
import com.templlo.service.review.feignClient.dto.ReservationStatus;
import com.templlo.service.review.feignClient.dto.UserData;
import com.templlo.service.review.event.dto.ReviewCreatedEventDto;
import com.templlo.service.review.event.internal.producer.ReviewInternalEventProducer;
import com.templlo.service.review.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "Create Review Service")
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CreateReviewService {

	private final ReviewRepository reviewRepository;
	private final UserClient userClient;
	private final ReservationClient reservationClient;
	private final ReviewInternalEventProducer internalEventProducer;

	@Transactional
	public void createReview(CreateReviewRequestDto request, String loginId) {
		UUID userId = getUserId(loginId);
		validateDuplicatedReview(request, userId);

		// ResponseEntity<ApiResponse<ReservationData>> reserveResponse = reservationClient.getReservationInfo(request.reservationId());
		// validateReservationStatus(reserveResponse);

		//Review review = createReview(request, reserveResponse, userId);
		UUID programId = UUID.fromString("1f4188f9-bca3-40c5-9e6f-b298e0354d20");
		Review review = Review.create(UUID.randomUUID(), programId, userId, request.rating(), request.content());
		reviewRepository.save(review);


		ReviewCreatedEventDto eventDto = ReviewCreatedEventDto.of(loginId, review);
		internalEventProducer.publishReviewCreated(eventDto);
	}

	private UUID getUserId(String loginId) {
		ApiResponse<UserData> userResponse = userClient.getUserInfo(loginId);
		return userResponse.data().id();
	}

	private void validateDuplicatedReview(CreateReviewRequestDto request, UUID userId) {
		reviewRepository.findByUserIdAndReservationId(userId, request.reservationId())
			.ifPresent(review -> {
				throw new DuplicatedReviewException();
			});
	}

	private static void validateReservationStatus(ResponseEntity<ApiResponse<ReservationData>> reserveResponse) {
		if (!ReservationStatus.COMPLETED.equals(reserveResponse.getBody().data().status())) {
			throw new NonReviewableException();
		}
	}

	private static Review createReview(CreateReviewRequestDto request,
		ResponseEntity<ApiResponse<ReservationData>> reserveResponse, UUID userId) {

		UUID reservationId = reserveResponse.getBody().data().reservationId();
		UUID programId = reserveResponse.getBody().data().programId();

		return Review.create(reservationId, programId, userId, request.rating(), request.content());
	}

}
