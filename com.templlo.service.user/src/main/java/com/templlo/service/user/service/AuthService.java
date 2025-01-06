package com.templlo.service.user.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.templlo.service.user.common.jwt.JwtTokenProvider;
import com.templlo.service.user.common.security.UserDetailsImpl;
import com.templlo.service.user.dto.LoginRequestDto;
import com.templlo.service.user.dto.TokenDto;
import com.templlo.service.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

	private final AuthenticationManager authenticationManager;
	private final JwtTokenProvider jwtTokenProvider;

	public TokenDto login(LoginRequestDto request) {
		Authentication authentication = authenticationManager.authenticate(
			new UsernamePasswordAuthenticationToken(request.loginId(), request.password()));

		UserDetailsImpl userDetails = (UserDetailsImpl)authentication.getPrincipal();
		User user = userDetails.getUser();

		String accessToken = jwtTokenProvider.createAccessToken(user.getLoginId(), user.getRole());
		String refreshToken = jwtTokenProvider.createRefreshToken(user.getLoginId(), user.getRole());

		return new TokenDto(accessToken, refreshToken);
	}
}
