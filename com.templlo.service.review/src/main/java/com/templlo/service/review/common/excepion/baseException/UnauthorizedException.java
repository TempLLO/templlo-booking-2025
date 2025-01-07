package com.templlo.service.review.common.excepion.baseException;

import com.templlo.service.review.common.excepion.ErrorCode;

import lombok.Getter;

@Getter
public class UnauthorizedException extends BaseException {

	public UnauthorizedException() {
		super(ErrorCode.UNAUTHORIZED);
	}
}
