package com.templlo.service.user.dto;

import java.util.UUID;

import com.templlo.service.user.entity.User;

public record UserResponseDto(
	UUID id,
	String loginId,
	String email,
	String userName,
	String nickName,
	String gender,
	String birth,
	String role,
	String phone,
	int reviewCount,
	boolean isDeleted
) {
	public static UserResponseDto from(User user) {
		return new UserResponseDto(
			user.getId(),
			user.getLoginId(),
			user.getEmail(),
			user.getUserName(),
			user.getNickName(),
			user.getGender().name(),
			user.getBirth(),
			user.getRole().name(),
			user.getPhone(),
			user.getReviewCount(),
			user.isDeleted()
		);
	}

}
