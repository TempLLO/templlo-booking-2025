package com.templlo.service.review.common.excepion.baseException;

import com.templlo.service.review.common.excepion.ErrorCode;

import lombok.Getter;

@Getter
public class CustomForbiddenException extends BaseException {

	public CustomForbiddenException() {
		super(ErrorCode.FORBIDDEN);
	}
}
