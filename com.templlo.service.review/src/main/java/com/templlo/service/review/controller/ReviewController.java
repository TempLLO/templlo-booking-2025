package com.templlo.service.review.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.templlo.service.review.common.response.ApiResponse;
import com.templlo.service.review.common.security.UserDetailsImpl;
import com.templlo.service.review.dto.CreateReviewRequestDto;
import com.templlo.service.review.service.ReviewService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {

	private final ReviewService reviewService;

	@PreAuthorize("hasAnyAuthority('MEMBER', 'MASTER')")
	@PostMapping
	public ApiResponse<Void> creatReview(@Valid @RequestBody CreateReviewRequestDto request,
		@AuthenticationPrincipal UserDetailsImpl userDetails) {
		reviewService.createReview(request, userDetails.getLoginId());
		return ApiResponse.basicSuccessResponse();
	}

}