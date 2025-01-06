package com.templlo.service.user.dto;

public record TokenDto(
	String accessToken,
	String refreshToken
) {
}
