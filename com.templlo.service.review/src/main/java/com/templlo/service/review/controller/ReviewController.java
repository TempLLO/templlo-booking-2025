package com.templlo.service.review.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.templlo.service.review.common.response.ApiResponse;
import com.templlo.service.review.common.response.BasicStatusCode;
import com.templlo.service.review.common.response.PageResponse;
import com.templlo.service.review.common.security.UserDetailsImpl;
import com.templlo.service.review.dto.CreateReviewRequestDto;
import com.templlo.service.review.dto.ReviewResponseDto;
import com.templlo.service.review.entity.Review;
import com.templlo.service.review.service.CreateReviewService;
import com.templlo.service.review.service.ReadReviewService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {

	private final CreateReviewService createReviewService;
	private final ReadReviewService readReviewService;

	@PreAuthorize("hasAuthority('MEMBER')")
	@PostMapping
	public ApiResponse<Void> creatReview(@Valid @RequestBody CreateReviewRequestDto request,
		@AuthenticationPrincipal UserDetailsImpl userDetails) {

		createReviewService.createReview(request, userDetails.getLoginId());
		return ApiResponse.basicSuccessResponse();
	}

	@PreAuthorize("hasAuthority('MEMBER')")
	@GetMapping
	public ApiResponse<PageResponse<ReviewResponseDto>> getMyReviews(
		@PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)
		Pageable pageable, @AuthenticationPrincipal UserDetailsImpl userDetails) {

		Page<Review> review = readReviewService.getReview(pageable, userDetails);
		return ApiResponse.of(BasicStatusCode.OK, ReviewResponseDto.pageOf(review));
	}
}