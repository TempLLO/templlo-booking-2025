package com.templlo.service.user.dto;

import jakarta.validation.constraints.NotNull;

public record LoginRequestDto(

	@NotNull(message = "아이디는 필수입니다. ")
	String loginId,

	@NotNull(message = "비밀번호는 필수입니다. ")
	String password) {
}