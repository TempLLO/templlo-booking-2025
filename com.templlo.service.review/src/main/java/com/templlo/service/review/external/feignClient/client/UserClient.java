package com.templlo.service.review.external.feignClient.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.templlo.service.review.common.response.ApiResponse;
import com.templlo.service.review.external.feignClient.FeignConfiguration;
import com.templlo.service.review.external.feignClient.dto.UserData;

@FeignClient(
	name = "user-service",
	configuration = FeignConfiguration.class)
public interface UserClient {

	@GetMapping("/api/users/{loginId}")
	ApiResponse<UserData> getUserInfo(@PathVariable(name = "loginId") String loginId);
}
