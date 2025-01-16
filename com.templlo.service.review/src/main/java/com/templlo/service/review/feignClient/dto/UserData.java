package com.templlo.service.review.feignClient.dto;

import java.util.UUID;

public record UserData(
	UUID id,
	String loginId,
	String email,
	String userName,
	String nickName,
	String gender,
	String birth,
	String role,
	String phone,
	int reviewCount
) {
}
