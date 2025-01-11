package com.templlo.service.review.dto;

import java.util.UUID;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateReviewRequestDto(

	@NotNull(message = "예약 id는 필수입니다. ")
	UUID reservationId,

	@NotNull(message = "내용을 입력해주세요. ")
	@Size(min = 20, message = "리뷰는 10자 이상 입력해주세요. ")
	String content,

	@NotNull(message = "별점을 선택해주세요. ")
	@DecimalMin(value = "1.0", message = "별점은 1.0 이상이어야 합니다. ")
	@DecimalMax(value = "5.0", message = "별점은 5.0 이하여야 합니다. ")
	@Digits(integer = 1, fraction = 1) // 정수부 한 자리, 소수점 한 자리 검증
	Double rating
) {
}
