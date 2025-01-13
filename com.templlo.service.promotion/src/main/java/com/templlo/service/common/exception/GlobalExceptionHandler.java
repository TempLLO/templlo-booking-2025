package com.templlo.service.common.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.templlo.service.common.response.ApiResponse;
import com.templlo.service.common.response.BasicStatusCode;
import com.templlo.service.common.response.StatusCode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	// 커스텀 예외 클래스 처리
	@ExceptionHandler(BaseException.class)
	public ResponseEntity<Object> handleCustomBaseException(BaseException e) {
		return handleException(e.getCode());
	}

	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<Object> handleAllRuntimeException(RuntimeException e) {
		log.error("Unhandled RuntimeException occurred: {}", e.getMessage(), e);
		return handleException(BasicStatusCode.INTERNAL_SERVER_ERROR, e.getMessage());
	}

	// MissingServletRequestParameterException 커스터마이징
	protected ResponseEntity<Object> handleMissingServletRequestParameter(
		MissingServletRequestParameterException ex,
		HttpHeaders headers,
		HttpStatus status,
		WebRequest request
	) {
		log.warn("Missing request parameter: {}", ex.getParameterName());
		ApiResponse<Void> response = ApiResponse.of(BasicStatusCode.BAD_REQUEST, ex.getMessage(), null);
		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}

	private ResponseEntity<Object> handleException(StatusCode code) {
		return ResponseEntity
			.status(code.getHttpStatus())
			.body(ApiResponse.of(code));
	}

	private ResponseEntity<Object> handleException(StatusCode code, String message) {
		ApiResponse<Void> errorResponse = ApiResponse.of(BasicStatusCode.INTERNAL_SERVER_ERROR, message, null);
		return ResponseEntity
			.status(code.getHttpStatus())
			.body(errorResponse);
	}
}
