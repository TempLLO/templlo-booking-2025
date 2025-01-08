package com.templlo.service.review.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.templlo.service.review.common.response.ApiResponse;
import com.templlo.service.review.common.security.UserDetailsImpl;
import com.templlo.service.review.entity.Review;
import com.templlo.service.review.external.feignClient.client.UserClient;
import com.templlo.service.review.external.feignClient.dto.UserData;
import com.templlo.service.review.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "Read Review Service")
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReadReviewService {

	private final ReviewRepository reviewRepository;
	private final UserClient userClient;

	public Page<Review> getReview(Pageable pageable, UserDetailsImpl userDetails) {
		ApiResponse<UserData> userResponse = userClient.getUserInfo(userDetails.getLoginId());
		UUID userId = userResponse.data().id();

		return reviewRepository.findByUserId(userId, pageable);

	}

	public Page<Review> getReviews(UUID programId, Pageable pageable) {
		return reviewRepository.findByProgramId(programId, pageable);
	}
}