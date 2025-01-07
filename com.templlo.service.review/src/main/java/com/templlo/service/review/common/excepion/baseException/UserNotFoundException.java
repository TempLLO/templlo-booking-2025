package com.templlo.service.review.common.excepion.baseException;

import com.templlo.service.review.common.excepion.ErrorCode;

import lombok.Getter;

@Getter
public class UserNotFoundException extends BaseException {

	public UserNotFoundException() {
		super(ErrorCode.USER_NOT_FOUND);
	}
}
