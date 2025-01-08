package com.templlo.service.review.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.templlo.service.review.common.response.ApiResponse;
import com.templlo.service.review.common.response.BasicStatusCode;
import com.templlo.service.review.common.response.PageResponse;
import com.templlo.service.review.common.security.UserDetailsImpl;
import com.templlo.service.review.common.util.PagingUtil;
import com.templlo.service.review.dto.CreateReviewRequestDto;
import com.templlo.service.review.dto.ReviewResponseDto;
import com.templlo.service.review.dto.UpdateReviewRequestDto;
import com.templlo.service.review.entity.Review;
import com.templlo.service.review.service.CreateReviewService;
import com.templlo.service.review.service.ReadReviewService;
import com.templlo.service.review.service.UpdateReviewService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {

	private final CreateReviewService createReviewService;
	private final ReadReviewService readReviewService;
	private final UpdateReviewService updateReviewService;

	@PreAuthorize("hasAuthority('MEMBER')")
	@PostMapping
	public ApiResponse<Void> creatReview(@Valid @RequestBody CreateReviewRequestDto request,
		@AuthenticationPrincipal UserDetailsImpl userDetails) {

		createReviewService.createReview(request, userDetails.getLoginId());
		return ApiResponse.basicSuccessResponse();
	}


	@PreAuthorize("hasAuthority('MEMBER')")
	@GetMapping("/me")
	public ApiResponse<PageResponse<ReviewResponseDto>> getMyReviews(
		@RequestParam(name = "page", defaultValue = "0") int page,
		@RequestParam(name = "size", defaultValue = "5") int size,
		@RequestParam(name = "sortBy", defaultValue = "createdAt") String sortBy,
		@RequestParam(name = "isAsc", defaultValue = "false") boolean isAsc,
		@AuthenticationPrincipal UserDetailsImpl userDetails) {

		Pageable pageable = PagingUtil.of(page, size, sortBy, isAsc);
		Page<Review> review = readReviewService.getReview(pageable, userDetails);
		return ApiResponse.of(BasicStatusCode.OK, ReviewResponseDto.pageOf(review));
	}

	@PreAuthorize("hasAnyAuthority('MEMBER', 'TEMPLE_ADMIN', 'MASTER')")
	@GetMapping
	public ApiResponse<PageResponse<ReviewResponseDto>> getReviews(
		@RequestParam(name = "programId") UUID programId,
		@RequestParam(name = "page", defaultValue = "0") int page,
		@RequestParam(name = "size", defaultValue = "10") int size,
		@RequestParam(name = "sortBy", defaultValue = "createdAt") String sortBy,
		@RequestParam(name = "isAsc", defaultValue = "false") boolean isAsc) {

		Pageable pageable = PagingUtil.of(page, size, sortBy, isAsc);
		Page<Review> reviews = readReviewService.getReviews(programId, pageable);
		return ApiResponse.of(BasicStatusCode.OK, ReviewResponseDto.pageOf(reviews));
	}

	@PreAuthorize("hasAuthority('MEMBER')")
	@PatchMapping("/{reviewId}")
	public ApiResponse<PageResponse<ReviewResponseDto>> updateReview(
		@PathVariable(name = "reviewId") UUID reviewId,
		@Valid @RequestBody UpdateReviewRequestDto request) {

		updateReviewService.updateReview(reviewId, request);
		return ApiResponse.of(BasicStatusCode.OK);
	}
}