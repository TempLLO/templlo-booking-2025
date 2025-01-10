package com.templlo.gateway.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum JwtValidType {
	VALID_TOKEN("유효한 JWT 토큰입니다."),

	INVALID_TOKEN("잘못된 JWT 토큰 입니다."),
	INVALID_SIGNATURE("유효하지 않는 JWT 서명 입니다."),
	EXPIRED_TOKEN("만료된 JWT token 입니다."),
	UNSUPPORTED_TOKEN("지원되지 않는 JWT 토큰 입니다."),
	EMPTY_TOKEN("JWT 토큰이 없습니다. "),

	INVALID_TOKEN_TYPE("요청에 적절한 토큰이 아닙니다. ");

	private final String description;
}