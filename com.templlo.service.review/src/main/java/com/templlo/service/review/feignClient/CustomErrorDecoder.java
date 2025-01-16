package com.templlo.service.review.feignClient;

import org.springframework.stereotype.Component;

import com.templlo.service.review.common.exception.baseException.BaseException;
import com.templlo.service.review.common.exception.baseException.CustomBadRequestException;
import com.templlo.service.review.common.exception.baseException.CustomFeignException;
import com.templlo.service.review.common.exception.baseException.CustomForbiddenException;
import com.templlo.service.review.common.exception.baseException.NotFoundException;
import com.templlo.service.review.common.exception.baseException.ReservationNotFoundException;
import com.templlo.service.review.common.exception.baseException.UnauthorizedException;
import com.templlo.service.review.common.exception.baseException.UserNotFoundException;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CustomErrorDecoder implements ErrorDecoder {

	@Override
	public Exception decode(String methodKey, Response response) {
		String requestUrl = response.request().url();
		log.error("Feign error occurred - methodKey: {}, url: {}, status: {}", methodKey, requestUrl,
			response.status());

		return switch (response.status()) {
			case 400 -> new CustomBadRequestException();
			case 401 -> new UnauthorizedException();
			case 403 -> new CustomForbiddenException();
			case 404 -> detailNotFoundException(requestUrl);
			case 500 -> new CustomFeignException();
			default -> new RuntimeException("Unknown External Service error");
		};
	}

	private static BaseException detailNotFoundException(String requestUrl) {
		if (requestUrl.contains("/api/users")) {
			return new UserNotFoundException();
		}
		if (requestUrl.contains("/api/reservations")) {
			return new ReservationNotFoundException();
		}
		return new NotFoundException();
	}

}