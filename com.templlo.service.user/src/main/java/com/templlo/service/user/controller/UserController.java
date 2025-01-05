package com.templlo.service.user.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.templlo.service.user.common.ApiResponse;
import com.templlo.service.user.dto.SignUpRequestDto;
import com.templlo.service.user.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@PostMapping("/sign-up")
	public ApiResponse<Void> join(@Valid @RequestBody SignUpRequestDto request) {
		userService.join(request);
		return ApiResponse.success();
	}
}
