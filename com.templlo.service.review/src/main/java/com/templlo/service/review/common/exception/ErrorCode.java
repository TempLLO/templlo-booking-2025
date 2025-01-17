package com.templlo.service.review.common.exception;

import org.springframework.http.HttpStatus;

import com.templlo.service.review.common.response.StatusCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode implements StatusCode {

	// 클라이언트 에러
	BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
	INVALID_RATING(HttpStatus.BAD_REQUEST, "평점은 1.0 이상 5.0 이하로만 입력 가능합니다. "),
	DUPLICATED_REVIEW(HttpStatus.BAD_REQUEST, "이미 작성된 후기가 존재합니다. "),
	NON_REVIEWABLE(HttpStatus.BAD_REQUEST, "리뷰 작성이 불가능합니다. "),

	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다."),

	FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 부족합니다."),

	NOT_FOUND(HttpStatus.NOT_FOUND, "정보를 조회할 수 없습니다. "),
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "유저 정보를 조회할 수 없습니다. "),
	REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "리뷰 정보를 조회할 수 없습니다. "),
	RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "예약 정보를 조회할 수 없습니다. "),

	// 서버 에러
	NPE(HttpStatus.INTERNAL_SERVER_ERROR, "NPE"),
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류입니다."),
	JSON_PARSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "json 파싱 에러가 발생했습니다. "),

	EXTERNAL_SERVICE_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "외부 서비스 연동 중 오류가 발생했습니다.");

	private final HttpStatus httpStatus;
	private final String message;

	@Override
	public String getName() {
		return this.name();
	}
}
