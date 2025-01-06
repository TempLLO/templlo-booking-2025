package com.templlo.service.user.common.response;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BasicStatusCode implements StatusCode {

	// 200
	OK(HttpStatus.OK, "API 요청에 성공했습니다"),

	// 클라이언트 에러
	BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다."),
	FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 부족합니다."),

	INVALID_USER(HttpStatus.BAD_REQUEST, "아이디 또는 비밀번호를 확인해주세요. "),
	DUPLICATE_LOGIN_ID(HttpStatus.BAD_REQUEST, "중복된 아이디입니다. "),

	NOT_FOUND(HttpStatus.NOT_FOUND, "요청 리소스를 찾을 수 없습니다."),
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 유저를 찾을 수 없습니다. "),

	// 서버 에러
	NPE(HttpStatus.INTERNAL_SERVER_ERROR, "NPE"),
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류입니다.");

	private final HttpStatus httpStatus;
	private final String message;

	@Override
	public String getName() {
		return this.name();
	}
}
