package com.templlo.service.review.feignClient.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.templlo.service.review.common.security.UserDetailsImpl;
import com.templlo.service.review.feignClient.CustomErrorDecoder;

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
				requestTemplate.header("X-Token", userDetails.getToken());
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
