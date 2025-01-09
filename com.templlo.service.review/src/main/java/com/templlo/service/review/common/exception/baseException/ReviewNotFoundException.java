package com.templlo.service.review.common.exception.baseException;

import com.templlo.service.review.common.exception.ErrorCode;

import lombok.Getter;

@Getter
public class ReviewNotFoundException extends BaseException {

	public ReviewNotFoundException() {
		super(ErrorCode.REVIEW_NOT_FOUND);
	}
}
