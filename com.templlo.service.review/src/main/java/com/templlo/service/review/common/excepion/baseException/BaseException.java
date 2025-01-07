package com.templlo.service.review.common.excepion.baseException;

import com.templlo.service.review.common.response.StatusCode;

import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {

	private final StatusCode code;

	public BaseException(StatusCode code) {
		this.code = code;
	}

}
