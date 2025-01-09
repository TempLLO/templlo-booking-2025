package com.templlo.service.review.common.exception.baseException;

import com.templlo.service.review.common.exception.ErrorCode;

import lombok.Getter;

@Getter
public class UnauthorizedException extends BaseException {

	public UnauthorizedException() {
		super(ErrorCode.UNAUTHORIZED);
	}
}
