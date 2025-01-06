package com.templlo.service.user.common.response;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BasicStatusCode implements StatusCode {

	OK(HttpStatus.OK, "API 요청에 성공했습니다"),

	BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다."),
	FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 부족합니다."),
	NOT_FOUND(HttpStatus.NOT_FOUND, "요청 리소스를 찾을 수 없습니다."),
	NPE(HttpStatus.INTERNAL_SERVER_ERROR, "NPE"),

	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류입니다.");

	private final HttpStatus httpStatus;
	private final String message;

	@Override
	public String getName() {
		return this.name();
	}
}
