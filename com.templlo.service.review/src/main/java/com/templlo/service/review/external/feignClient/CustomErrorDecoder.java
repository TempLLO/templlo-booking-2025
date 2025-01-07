package com.templlo.service.review.external.feignClient;

import org.springframework.stereotype.Component;

import com.templlo.service.review.common.excepion.baseException.CustomBadRequestException;
import com.templlo.service.review.common.excepion.baseException.CustomFeignException;
import com.templlo.service.review.common.excepion.baseException.CustomForbiddenException;
import com.templlo.service.review.common.excepion.baseException.UnauthorizedException;
import com.templlo.service.review.common.excepion.baseException.UserNotFoundException;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CustomErrorDecoder implements ErrorDecoder {

	@Override
	public Exception decode(String methodKey, Response response) {

		log.error("Feign error occurred - methodKey: {}, url: {}, status: {}", methodKey, response.request().url(), response.status());

		return switch (response.status()) {
			case 400 -> new CustomBadRequestException();
			case 401 -> new UnauthorizedException();
			case 403 -> new CustomForbiddenException();
			case 404 -> new UserNotFoundException();
			case 500 -> new CustomFeignException();
			default -> new RuntimeException("Unknown External Service error");
		};
	}

}