package com.templlo.service.review.common.excepion.baseException;

import com.templlo.service.review.common.excepion.ErrorCode;

import lombok.Getter;

@Getter
public class ReviewNotFoundException extends BaseException {

	public ReviewNotFoundException() {
		super(ErrorCode.REVIEW_NOT_FOUND);
	}
}
