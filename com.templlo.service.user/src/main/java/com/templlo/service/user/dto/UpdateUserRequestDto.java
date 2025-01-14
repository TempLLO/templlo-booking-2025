package com.templlo.service.user.dto;

import com.templlo.service.user.entity.enums.Gender;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record UpdateUserRequestDto(

	@NotNull(message = "비밀번호는 필수입니다. ")
	@Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,15}$",
		message = "비밀번호는 8~15자의 영문 대/소문자, 숫자, 특수문자를 포함해야 합니다. ")
	String password,

	@NotNull(message = "이메일을 입력해주세요. ")
	@Email(message = "이메일 형식에 맞지않습니다. ")
	String email,

	@NotNull(message = "닉네임을 입력해주세요. ")
	String nickName,

	@NotNull(message = "이름을 입력해주세요. ")
	String userName,

	@NotNull(message = "성별을 입력해주세요. ")
	Gender gender,

	@Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$",
		message = "생년월일은 YYYY-MM-DD 형식이어야 합니다. ")
	String birth,

	@Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다. ")
	String phone

) {
}
