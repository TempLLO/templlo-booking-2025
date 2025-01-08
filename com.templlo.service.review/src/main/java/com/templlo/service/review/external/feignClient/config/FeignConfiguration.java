package com.templlo.service.review.external.feignClient.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.templlo.service.review.common.security.UserDetailsImpl;
import com.templlo.service.review.external.feignClient.CustomErrorDecoder;

import feign.Logger;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;

@Configuration
public class FeignConfiguration {

	@Bean
	public RequestInterceptor requestInterceptor() {
		return requestTemplate -> {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			if (authentication != null && authentication.isAuthenticated()) {
				UserDetailsImpl userDetails = (UserDetailsImpl)authentication.getPrincipal();
				requestTemplate.header("X-Login-Id", userDetails.getLoginId());
				requestTemplate.header("X-User-Role", userDetails.getRole());
			}

		};
	}

	@Bean
	Logger.Level feignLoggerLevel() {
		return Logger.Level.FULL;
	}

	@Bean
	public ErrorDecoder errorDecoder() {
		return new CustomErrorDecoder();
	}
}
