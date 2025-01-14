package com.templlo.service.user.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.templlo.service.user.common.response.ApiResponse;
import com.templlo.service.user.common.response.BasicStatusCode;
import com.templlo.service.user.common.security.GatewayUserDetailsImpl;
import com.templlo.service.user.dto.SignUpRequestDto;
import com.templlo.service.user.dto.UpdateUserRequestDto;
import com.templlo.service.user.dto.UserResponseDto;
import com.templlo.service.user.service.CreateUserService;
import com.templlo.service.user.service.ModifyUserService;
import com.templlo.service.user.service.ReadUserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

	private final CreateUserService createUserService;
	private final ReadUserService readUserService;
	private final ModifyUserService modifyUserService;

	@PostMapping("/sign-up")
	public ApiResponse<Void> join(@Valid @RequestBody SignUpRequestDto request) {
		createUserService.join(request);
		return ApiResponse.basicSuccessResponse();
	}

	@PreAuthorize("hasAnyAuthority('MEMBER', 'TEMPLE_ADMIN', 'MASTER')")
	@GetMapping("/{loginId}")
	public ApiResponse<UserResponseDto> getUser(@PathVariable(name = "loginId") String loginId,
		@AuthenticationPrincipal GatewayUserDetailsImpl userDetails) {

		UserResponseDto responseDto = readUserService.getUser(loginId, userDetails.getLoginId(),
			userDetails.getUserRole());
		return ApiResponse.of(BasicStatusCode.OK, responseDto);
	}

	@PreAuthorize("hasAnyAuthority('MEMBER', 'TEMPLE_ADMIN', 'MASTER')")
	@PatchMapping("/me")
	public ApiResponse<Void> updateUser(@Valid @RequestBody UpdateUserRequestDto request,
		@AuthenticationPrincipal GatewayUserDetailsImpl userDetails) {

		modifyUserService.updateUser(request, userDetails.getAccessToken(), userDetails.getLoginId());
		return ApiResponse.basicSuccessResponse();
	}

	@PreAuthorize("hasAnyAuthority('MEMBER', 'TEMPLE_ADMIN', 'MASTER')")
	@DeleteMapping("/me")
	public ApiResponse<Void> deleteUser(@AuthenticationPrincipal GatewayUserDetailsImpl userDetails) {
		modifyUserService.deleteUser(userDetails.getLoginId(), userDetails.getAccessToken());
		return ApiResponse.basicSuccessResponse();
	}
}
