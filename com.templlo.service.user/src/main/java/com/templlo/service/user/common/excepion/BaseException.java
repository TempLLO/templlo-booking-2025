package com.templlo.service.user.common.excepion;

import com.templlo.service.user.common.response.StatusCode;

import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {

	private final StatusCode code;

	public BaseException(StatusCode code) {
		this.code = code;
	}

}
