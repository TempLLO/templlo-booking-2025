package com.templlo.service.user.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.templlo.service.user.common.excepion.BaseException;
import com.templlo.service.user.common.response.BasicStatusCode;
import com.templlo.service.user.dto.SignUpRequestDto;
import com.templlo.service.user.dto.UserResponseDto;
import com.templlo.service.user.entity.User;
import com.templlo.service.user.entity.enums.UserRole;
import com.templlo.service.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Transactional
	public void join(SignUpRequestDto request) {
		validateDuplicateLoginId(request.loginId());
		User user = User.create(request.loginId(), passwordEncoder.encode(request.password()), request.email(),
			request.userName(), request.nickName(), request.gender(), request.birth(), request.role(), request.phone());
		userRepository.save(user);
	}

	public UserResponseDto getUser(String loginId, String requestLoginId, UserRole requestUserRole) {
		User targetUser = findUserByLoginId(loginId);
		validateUserAccess(targetUser, requestLoginId, requestUserRole);
		return UserResponseDto.from(targetUser);
	}

	private void validateUserAccess(User targetUser, String requestLoginId, UserRole requestUserRole) {
		// MASTER : 모든 유저에 대해 정보 조회 가능
		if (UserRole.MASTER == requestUserRole) {
			return;
		}

		// 다른 권한은 본인 계정만 조회 가능
		if (!targetUser.getLoginId().equals(requestLoginId)) {
			throw new BaseException(BasicStatusCode.FORBIDDEN);
		}
	}

	private User findUserByLoginId(String loginId) {
		return userRepository.findByLoginId(loginId)
			.orElseThrow(() -> new BaseException(BasicStatusCode.USER_NOT_FOUND));
	}

	private void validateDuplicateLoginId(String loginId) {
		if (userRepository.existsByLoginId(loginId)) {
			throw new BaseException(BasicStatusCode.DUPLICATE_LOGIN_ID);
		}
	}

}
