package com.templlo.service.review.common.excepion.baseException;

import com.templlo.service.review.common.excepion.ErrorCode;

import lombok.Getter;

@Getter
public class DuplicatedReviewException extends BaseException {

	public DuplicatedReviewException() {
		super(ErrorCode.DUPLICATED_REVIEW);
	}
}
