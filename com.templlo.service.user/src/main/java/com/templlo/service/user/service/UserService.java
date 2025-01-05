package com.templlo.service.user.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.templlo.service.user.dto.SignUpRequestDto;
import com.templlo.service.user.entity.User;
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
		checkDuplicatedLoginId(request.loginId());
		User user = User.create(request.loginId(), passwordEncoder.encode(request.password()), request.email(),
			request.userName(), request.nickName(), request.gender(), request.birth(), request.role(), request.phone());
		userRepository.save(user);
	}

	private void checkDuplicatedLoginId(String loginId) {
		if (userRepository.existsByLoginId(loginId)) {
			throw new RuntimeException(); // TODO 예외처리
		}
	}

}
