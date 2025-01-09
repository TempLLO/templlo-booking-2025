package com.templlo.service.review.common.exception.baseException;

import com.templlo.service.review.common.exception.ErrorCode;

import lombok.Getter;

@Getter
public class CustomForbiddenException extends BaseException {

	public CustomForbiddenException() {
		super(ErrorCode.FORBIDDEN);
	}
}
