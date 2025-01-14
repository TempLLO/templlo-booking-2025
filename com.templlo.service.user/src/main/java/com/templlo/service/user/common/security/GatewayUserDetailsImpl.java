package com.templlo.service.user.common.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.templlo.service.user.entity.enums.UserRole;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class GatewayUserDetailsImpl implements UserDetails {

	private final String loginId;
	private final String role;
	private final String accessToken;

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority(role));
	}

	@Override
	public String getPassword() {
		return null;
	}

	@Override
	public String getUsername() {  // loginId 반환
		return loginId;
	}

	public UserRole getUserRole() {
		return UserRole.fromString(role);
	}

	public String getAccessToken() {
		return accessToken;
	}
}
