package com.templlo.service.review.common.exception.baseException;

import com.templlo.service.review.common.exception.ErrorCode;

import lombok.Getter;

@Getter
public class CustomFeignException extends BaseException {

	public CustomFeignException() {
		super(ErrorCode.EXTERNAL_SERVICE_ERROR);
	}
}
