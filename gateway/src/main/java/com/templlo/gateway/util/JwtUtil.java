package com.templlo.gateway.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "== Gateway: Jwt Util ==")
@Component
public class JwtUtil {

	private static final String TOKEN_TYPE = "type";
	private static final String ACCESS_TOKEN = "access";
	private static final String REFRESH_TOKEN = "refresh";

	@Value("${jwt.secret}")
	private String key;

	public JwtValidType validateToken(String accessToken) {

		if (!StringUtils.hasText(accessToken)) {
			return JwtValidType.EMPTY_TOKEN;
		}

		try {
			getClaims(accessToken);
			return JwtValidType.VALID_TOKEN;
		} catch (SecurityException | MalformedJwtException | SignatureException e) {
			return JwtValidType.INVALID_SIGNATURE;
		} catch (ExpiredJwtException e) {
			return JwtValidType.EXPIRED_TOKEN;
		} catch (UnsupportedJwtException e) {
			return JwtValidType.UNSUPPORTED_TOKEN;
		} catch (IllegalArgumentException e) {
			return JwtValidType.EMPTY_TOKEN;
		}
	}

	public Claims getClaims(String accessToken) {
		return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody();
	}

	public boolean isAccessToken(String token) {
		return ACCESS_TOKEN.equals(getClaims(token).get(TOKEN_TYPE));
	}

	public boolean isRefreshToken(String token) {
		return REFRESH_TOKEN.equals(getClaims(token).get(TOKEN_TYPE));
	}
}
