package com.templlo.service.user.common.jwt;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.templlo.service.user.entity.enums.UserRole;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "JWT Provider")
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

	private static final String CLAIM_LOGIN_ID = "loginId";
	private static final String CLAIM_USER_ROLE = "role";

	private static final String TOKEN_TYPE = "type";
	private static final String ACCESS_TOKEN = "access";
	private static final String REFRESH_TOKEN = "refresh";

	private static final long ACCESS_TOKEN_EXPIRE_TIME = 30 * 60 * 1000L;           // 30분
	private static final long REFRESH_TOKEN_EXPIRE_TIME = 24 * 60 * 60 * 1000L;    // 24시간

	@Value("${jwt.secret}")
	private String secretKey;
	private Key key;
	private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

	@PostConstruct
	public void init() {
		byte[] bytes = Base64.getDecoder().decode(secretKey);
		key = Keys.hmacShaKeyFor(bytes);
	}

	public String createAccessToken(String loginId, UserRole role) {
		return generateToken(loginId, role, ACCESS_TOKEN, ACCESS_TOKEN_EXPIRE_TIME);
	}

	public String createRefreshToken(String loginId, UserRole role) {
		return generateToken(loginId, role, REFRESH_TOKEN, REFRESH_TOKEN_EXPIRE_TIME);
	}

	private String generateToken(String loginId, UserRole role, String tokenType, long expireTime) {
		final Claims claims = Jwts.claims()
			.setIssuedAt(new Date(System.currentTimeMillis()))
			.setExpiration(new Date(System.currentTimeMillis() + expireTime));

		claims.put(CLAIM_LOGIN_ID, loginId);
		claims.put(CLAIM_USER_ROLE, role);
		claims.put(TOKEN_TYPE, tokenType);

		return Jwts.builder()
			.setHeaderParam(Header.TYPE, Header.JWT_TYPE)
			.setClaims(claims)
			.signWith(key, signatureAlgorithm)
			.compact();
	}
}
