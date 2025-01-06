package com.templlo.service.user.common.excepion;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.templlo.service.user.common.response.ApiResponse;
import com.templlo.service.user.common.response.BasicStatusCode;
import com.templlo.service.user.common.response.StatusCode;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	@ExceptionHandler(BaseException.class)
	public ResponseEntity<Object> handleCustomBaseException(BaseException e){
		return handleException(e.getCode());
	}

	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<Object> handleAllRuntimeException(RuntimeException e){
		return handleException(BasicStatusCode.INTERNAL_SERVER_ERROR, e.getMessage());
	}

	@ExceptionHandler(NullPointerException.class)
	public ResponseEntity<Object> handleNullPointerException(NullPointerException e) {
		return handleException(BasicStatusCode.NPE, e.getMessage());
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Object> illegalArgumentExceptionHandler(IllegalArgumentException e) {
		return handleException(BasicStatusCode.BAD_REQUEST, e.getMessage());
	}

	private ResponseEntity<Object> handleException(StatusCode code){
		return ResponseEntity
			.status(code.getHttpStatus())
			.body(ApiResponse.of(code));
	}

	private ResponseEntity<Object> handleException(StatusCode code, String message){
		ApiResponse errorResponse = ApiResponse.of(BasicStatusCode.INTERNAL_SERVER_ERROR, message);
		return ResponseEntity
			.status(code.getHttpStatus())
			.body(errorResponse);
	}
}