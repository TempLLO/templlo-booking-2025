package com.templlo.service.review.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.templlo.service.review.common.exception.baseException.DuplicatedReviewException;
import com.templlo.service.review.common.response.ApiResponse;
import com.templlo.service.review.dto.CreateReviewRequestDto;
import com.templlo.service.review.entity.Review;
import com.templlo.service.review.external.feignClient.client.ReservationClient;
import com.templlo.service.review.external.feignClient.client.UserClient;
import com.templlo.service.review.external.feignClient.dto.UserData;
import com.templlo.service.review.external.kafka.producer.dto.ReviewCreatedEventDto;
import com.templlo.service.review.external.kafka.producer.ReviewEventProducer;
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
	private final ReviewEventProducer eventProducer;

	@Transactional
	public void createReview(CreateReviewRequestDto request, String loginId) {
		ApiResponse<UserData> userResponse = userClient.getUserInfo(loginId);
		UUID userId = userResponse.data().id();

		// TODO 검증로직 수정 필요 : 예약에 대한 후기 작성 관점으로 고려
		//validateDuplicatedReview(request, userId);

		// TODO 예약내역 검증 필요 feignClient
		// reservationClient.getReservationInfo(userId);

		Review review = Review.create(request.programId(), userId, request.rating(), request.content());
		reviewRepository.save(review);

		// TODO 트랜잭션이 순차적으로 진행되는건지 확인이 필요(트러블슈팅 사항 : DB 트랜잭션이 종료 되기전에 이벤트가 발행된다면?) #1
		ReviewCreatedEventDto eventDto = ReviewCreatedEventDto.of(loginId, review);
		eventProducer.publishReviewCreated(eventDto);
	}

	private void validateDuplicatedReview(CreateReviewRequestDto request, UUID userId) {
		reviewRepository.findByUserIdAndProgramId(userId, request.programId())
			.ifPresent(review -> {
				throw new DuplicatedReviewException();
			});
	}

}
