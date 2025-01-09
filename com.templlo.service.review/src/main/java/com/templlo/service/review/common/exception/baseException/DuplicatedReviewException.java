package com.templlo.service.review.common.exception.baseException;

import com.templlo.service.review.common.exception.ErrorCode;

import lombok.Getter;

@Getter
public class DuplicatedReviewException extends BaseException {

	public DuplicatedReviewException() {
		super(ErrorCode.DUPLICATED_REVIEW);
	}
}
