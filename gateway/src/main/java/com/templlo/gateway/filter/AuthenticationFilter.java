package com.templlo.gateway.filter;

import java.util.List;
import java.util.Map;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.templlo.gateway.util.JwtUtil;
import com.templlo.gateway.util.JwtValidType;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j(topic = " Gateway: Authentication Filter ")
@Component
@RequiredArgsConstructor
public class AuthenticationFilter implements GlobalFilter {

	private final static String BEARER_PREFIX = "Bearer ";
	private static final String CLAIM_LOGIN_ID = "loginId";
	private static final String CLAIM_USER_ROLE = "role";
	private static final String REFRESH_TOKEN = "refresh";

	private final JwtUtil jwtUtil;

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		String path = exchange.getRequest().getURI().getPath();
		log.info("Request path : " + path);

		if (path.equals("/api/auth/login") || path.equals("/api/users/sign-up")) {
			return chain.filter(exchange);
		}

		return path.equals("/api/auth/reissue")
			? handleReissueRequest(exchange, chain)
			: handleProtectedRequest(exchange, chain);
	}

	private Mono<Void> handleReissueRequest(ServerWebExchange exchange, GatewayFilterChain chain) {
		String refreshToken = getRefreshTokenFromCookie(exchange.getRequest());

		if (refreshToken == null) {
			return handleInvalidToken(exchange, JwtValidType.EMPTY_TOKEN);
		}

		JwtValidType resultJwtType = jwtUtil.validateToken(refreshToken);
		if (resultJwtType != JwtValidType.VALID_TOKEN) {
			return handleInvalidToken(exchange, resultJwtType);
		}

		if (!jwtUtil.isRefreshToken(refreshToken)) {
			return handleInvalidToken(exchange, JwtValidType.INVALID_TOKEN_TYPE);
		}

		return processValidToken(exchange, chain, refreshToken);
	}

	private Mono<Void> handleProtectedRequest(ServerWebExchange exchange, GatewayFilterChain chain) {
		String accessToken = getAccessTokenFromHeader(exchange);

		if (accessToken == null) {
			return handleInvalidToken(exchange, JwtValidType.EMPTY_TOKEN);
		}

		JwtValidType validationType = jwtUtil.validateToken(accessToken);
		if (validationType != JwtValidType.VALID_TOKEN) {
			return handleInvalidToken(exchange, validationType);
		}

		if (!jwtUtil.isAccessToken(accessToken)) {
			return handleInvalidToken(exchange, JwtValidType.INVALID_TOKEN_TYPE);
		}

		return processValidToken(exchange, chain, accessToken);
	}

	private String getRefreshTokenFromCookie(ServerHttpRequest request) {
		MultiValueMap<String, HttpCookie> cookies = request.getCookies();
		if (cookies.containsKey(REFRESH_TOKEN)) {
			List<HttpCookie> refreshTokenCookies = cookies.get(REFRESH_TOKEN);
			if (!refreshTokenCookies.isEmpty()) {
				return refreshTokenCookies.get(0).getValue();
			}
		}
		return null;
	}

	private Mono<Void> processValidToken(ServerWebExchange exchange, GatewayFilterChain chain, String token) {
		Claims claims = jwtUtil.getClaims(token);
		String loginId = String.valueOf(claims.get(CLAIM_LOGIN_ID));
		String role = String.valueOf(claims.get(CLAIM_USER_ROLE));

		ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
			.header("X-Login-Id", loginId)
			.header("X-User-Role", role)
			.header("X-Token", token)
			.build();

		log.info("Gateway User loginId : {} , role : {}", loginId, role);

		exchange = exchange.mutate().request(modifiedRequest).build();

		exchange.getRequest().getHeaders().forEach((name, values) -> {
			log.debug("Header: {} = {}", name, values);
		});

		return chain.filter(exchange);
	}

	private Mono<Void> handleInvalidToken(ServerWebExchange exchange, JwtValidType validationType) {
		sendErrorResponse(exchange, validationType);
		return exchange.getResponse().setComplete();
	}

	private void sendErrorResponse(ServerWebExchange exchange, JwtValidType resultJwtType) {
		ServerHttpResponse response = exchange.getResponse();
		String msg;

		response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

		switch (resultJwtType) {
			case INVALID_SIGNATURE:
				msg = JwtValidType.INVALID_SIGNATURE.getDescription();
				response.setStatusCode(HttpStatus.UNAUTHORIZED);
				break;
			case EXPIRED_TOKEN:
				msg = JwtValidType.EXPIRED_TOKEN.getDescription();
				response.setStatusCode(HttpStatus.FORBIDDEN);
				break;
			case UNSUPPORTED_TOKEN:
				msg = JwtValidType.UNSUPPORTED_TOKEN.getDescription();
				response.setStatusCode(HttpStatus.UNAUTHORIZED);
				break;
			case EMPTY_TOKEN:
				msg = JwtValidType.EMPTY_TOKEN.getDescription();
				response.setStatusCode(HttpStatus.UNAUTHORIZED);
				break;
			case INVALID_TOKEN_TYPE:
				msg = JwtValidType.INVALID_TOKEN_TYPE.getDescription();
				response.setStatusCode(HttpStatus.UNAUTHORIZED);
				break;
			default:
				msg = JwtValidType.INVALID_TOKEN.getDescription();
				response.setStatusCode(HttpStatus.UNAUTHORIZED);
		}

		Map<String, Object> errorResponse = Map.of("error", msg);

		try {
			byte[] responseBody = new ObjectMapper().writeValueAsBytes(errorResponse);
			DataBuffer buffer = response.bufferFactory().wrap(responseBody);
			response.writeWith(Mono.just(buffer)).subscribe();
		} catch (JsonProcessingException e) {
			log.error("Error writing response", e);
		}
	}

	private String getAccessTokenFromHeader(ServerWebExchange exchange) {
		String bearerToken = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
			return bearerToken.substring(BEARER_PREFIX.length());
		}
		return null;
	}

}
