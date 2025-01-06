package com.templlo.service.user.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.templlo.service.user.common.response.ApiResponse;
import com.templlo.service.user.common.response.BasicStatusCode;
import com.templlo.service.user.common.security.GatewayUserDetailsImpl;
import com.templlo.service.user.dto.SignUpRequestDto;
import com.templlo.service.user.dto.UserResponseDto;
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
		return ApiResponse.basicSuccessResponse();
	}

	@GetMapping("/{loginId}")
	public ApiResponse<UserResponseDto> getUser(@PathVariable(name = "loginId") String loginId,
		@AuthenticationPrincipal GatewayUserDetailsImpl userDetails) {

		UserResponseDto responseDto = userService.getUser(loginId, userDetails.getLoginId(), userDetails.getUserRole());
		return ApiResponse.of(BasicStatusCode.OK, responseDto);
	}
}
