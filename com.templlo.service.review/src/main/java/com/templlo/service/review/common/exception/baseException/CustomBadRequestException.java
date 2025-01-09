package com.templlo.service.review.common.exception.baseException;

import com.templlo.service.review.common.exception.ErrorCode;

import lombok.Getter;

@Getter
public class CustomBadRequestException extends BaseException {

	public CustomBadRequestException() {
		super(ErrorCode.BAD_REQUEST);
	}
}
