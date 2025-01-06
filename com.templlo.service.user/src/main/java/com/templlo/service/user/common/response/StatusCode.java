package com.templlo.service.user.common.response;

import org.springframework.http.HttpStatus;

public interface StatusCode {
	String getName();
	HttpStatus getHttpStatus();
	String getMessage();
}
