package com.templlo.service.user.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.templlo.service.user.common.exception.BaseException;
import com.templlo.service.user.common.jwt.JwtTokenProvider;
import com.templlo.service.user.common.response.BasicStatusCode;
import com.templlo.service.user.common.security.UserDetailsImpl;
import com.templlo.service.user.dto.LoginRequestDto;
import com.templlo.service.user.dto.TokenDto;
import com.templlo.service.user.entity.User;
import com.templlo.service.user.entity.enums.UserRole;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

	private final AuthenticationManager authenticationManager;
	private final JwtTokenProvider jwtTokenProvider;

	public TokenDto login(LoginRequestDto request) {
		try {
			Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(request.loginId(), request.password()));

			UserDetailsImpl userDetails = (UserDetailsImpl)authentication.getPrincipal();
			User user = userDetails.getUser();

			String accessToken = jwtTokenProvider.createAccessToken(user.getLoginId(), user.getRole());
			String refreshToken = jwtTokenProvider.createRefreshToken(user.getLoginId(), user.getRole());

			return new TokenDto(accessToken, refreshToken);

		} catch (BadCredentialsException e) {
			throw new BaseException(BasicStatusCode.INVALID_USER);
		}

	}

	public TokenDto reissue(String loginId, String role) {
		String accessToken = jwtTokenProvider.createAccessToken(loginId, UserRole.fromString(role));
		String refreshToken = jwtTokenProvider.createRefreshToken(loginId, UserRole.fromString(role));

		return new TokenDto(accessToken, refreshToken);
	}
}
