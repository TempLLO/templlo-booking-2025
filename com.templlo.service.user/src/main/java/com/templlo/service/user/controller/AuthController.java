package com.templlo.service.user.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.templlo.service.user.common.response.ApiResponse;
import com.templlo.service.user.common.response.BasicStatusCode;
import com.templlo.service.user.common.security.GatewayUserDetailsImpl;
import com.templlo.service.user.dto.LoginRequestDto;
import com.templlo.service.user.dto.TokenDto;
import com.templlo.service.user.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@PostMapping("/login")
	public ApiResponse<TokenDto> login(@Valid @RequestBody LoginRequestDto request) {
		TokenDto tokenDto = authService.login(request);
		return ApiResponse.of(BasicStatusCode.OK, tokenDto);
	}

	@PostMapping("/reissue")
	public ApiResponse<TokenDto> reissue(@AuthenticationPrincipal GatewayUserDetailsImpl userDetails) {
		TokenDto tokenDto = authService.reissue(userDetails.getLoginId(), userDetails.getRole(),
			userDetails.getAccessToken());
		return ApiResponse.of(BasicStatusCode.REISSUE_TOKEN, tokenDto);
	}

	@PostMapping("/logout")
	public ApiResponse<Void> logout(@AuthenticationPrincipal GatewayUserDetailsImpl userDetails,
		@CookieValue(name = "refresh") String refreshToken) {
		authService.logout(userDetails.getAccessToken(), refreshToken, userDetails.getLoginId());
		return ApiResponse.of(BasicStatusCode.LOGOUT);
	}
}
