package com.templlo.service.user.common.jwt.util;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.templlo.service.user.common.exception.BaseException;
import com.templlo.service.user.common.response.BasicStatusCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "Token Blacklist Util")
@Component
@RequiredArgsConstructor
public class TokenRedisUtil {

	private final RedisTemplate<String, String> redisTemplate;
	private final JwtUtil jwtUtil;
	private static final String BLACKLIST_PREFIX = "blacklist:";

	public void addToBlacklist(String accessToken, String loginId, String value) {
		deleteRefreshToken(loginId);

		String accessBlacklistKey = BLACKLIST_PREFIX + accessToken;
		log.info("loginId {} 의 accessToken 블랙리스트 등록", loginId);
		redisTemplate.opsForValue()
			.set(accessBlacklistKey, value, jwtUtil.getTokenExpiration(accessToken), TimeUnit.MILLISECONDS);
	}

	public void deleteRefreshToken(String loginId) {
		log.info("loginId {} 의 저장된 refreshToken 삭제", loginId);
		redisTemplate.delete(loginId);
	}

	public String getSavedRefreshToken(String loginId) {
		String savedRefreshToken = redisTemplate.opsForValue().get(loginId);
		if (savedRefreshToken == null) {
			throw new BaseException(BasicStatusCode.REFRESH_TOKEN_NOT_FOUND);
		}

		return savedRefreshToken;
	}
}
