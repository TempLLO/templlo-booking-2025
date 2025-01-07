package com.templlo.service.review.common.excepion.baseException;

import com.templlo.service.review.common.excepion.ErrorCode;

import lombok.Getter;

@Getter
public class CustomBadRequestException extends BaseException {

	public CustomBadRequestException() {
		super(ErrorCode.BAD_REQUEST);
	}
}
