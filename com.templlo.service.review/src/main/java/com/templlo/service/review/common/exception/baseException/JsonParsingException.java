package com.templlo.service.review.common.exception.baseException;

import com.templlo.service.review.common.exception.ErrorCode;

import lombok.Getter;

@Getter
public class JsonParsingException extends BaseException {

	public JsonParsingException() {
		super(ErrorCode.JSON_PARSING_ERROR);
	}
}
