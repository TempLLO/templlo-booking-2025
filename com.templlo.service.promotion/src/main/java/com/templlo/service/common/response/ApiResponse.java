package com.templlo.service.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
	String status,
	String code,
	String message,
	T data
) {

	public static ApiResponse<Void> basicSuccessResponse() {
		return ApiResponse.of(BasicStatusCode.OK);
	}

	public static <T> ApiResponse<T> of(StatusCode statusCode) {
		return ApiResponse.of(statusCode, null);
	}

	public static <T> ApiResponse<T> of(StatusCode statusCode, T data) {
		return ApiResponse.of(statusCode, statusCode.getMessage(), data);
	}

	public static <T> ApiResponse<T> of(StatusCode statusCode, String message, T data) {
		return ApiResponse.<T>builder()
			.status(String.valueOf(statusCode.getHttpStatus().value()))
			.code(statusCode.getName())
			.message(message)
			.data(data)
			.build();
	}

	public static <T> ApiResponse<T> ofSuccess(T data) {
		return ApiResponse.<T>builder()
			.status(String.valueOf(BasicStatusCode.OK.getHttpStatus().value())) // HTTP 상태 코드 값
			.code(BasicStatusCode.OK.getName()) // 상태 코드 이름
			.message(BasicStatusCode.OK.getMessage()) // 기본 메시지
			.data(data)
			.build();
	}

}