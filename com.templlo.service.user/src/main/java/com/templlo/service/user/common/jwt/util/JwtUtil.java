package com.templlo.service.user.common.jwt.util;

import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "Jwt Util")
@Component
public class JwtUtil {

	@Value("${jwt.secret}")
	private String key;

	public Claims getClaims(String accessToken) {
		return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody();
	}

	public long getTokenExpiration(String token) {
		Date expiration = getClaims(token).getExpiration();
		return expiration.getTime() - System.currentTimeMillis();
	}
}
