package com.templlo.service.review.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.templlo.service.review.common.excepion.ErrorCode;
import com.templlo.service.review.common.excepion.baseException.BaseException;
import com.templlo.service.review.common.excepion.baseException.DuplicatedReviewException;
import com.templlo.service.review.common.response.ApiResponse;
import com.templlo.service.review.dto.CreateReviewRequestDto;
import com.templlo.service.review.entity.Review;
import com.templlo.service.review.external.feignClient.client.ReservationClient;
import com.templlo.service.review.external.feignClient.client.UserClient;
import com.templlo.service.review.external.feignClient.dto.UserData;
import com.templlo.service.review.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "Review Service")
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

	private final ReviewRepository reviewRepository;
	private final UserClient userClient;
	private final ReservationClient reservationClient;

	@Transactional
	public void createReview(CreateReviewRequestDto request, String loginId) {
		ApiResponse<UserData> userResponse = userClient.getUserInfo(loginId);
		UUID userId = userResponse.data().id();

		validateDuplicatedReview(request, userId);

		// TODO 예약내역 검증 필요 feignClient
		// reservationClient.getReservationInfo(userId);

		Review review = Review.create(request.programId(), userId, request.rating(), request.content());
		reviewRepository.save(review);

		// TODO 리뷰 작성 이벤트 발행

	}

	private void validateDuplicatedReview(CreateReviewRequestDto request, UUID userId) {
		reviewRepository.findByUserIdAndProgramId(userId, request.programId())
			.ifPresent(review -> {
				throw new DuplicatedReviewException();
			});
	}
}
