package com.templlo.service.review.common.response;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BasicStatusCode implements StatusCode {

	// 200
	OK(HttpStatus.OK, "API 요청에 성공했습니다");

	private final HttpStatus httpStatus;
	private final String message;

	@Override
	public String getName() {
		return this.name();
	}
}
