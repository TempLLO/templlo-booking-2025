package com.templlo.gateway.filter;

import java.util.Map;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
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

	private final JwtUtil jwtUtil;

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		String path = exchange.getRequest().getURI().getPath();
		log.info("Request path : " + path);
		if (path.equals("/api/auth/login") || path.equals("/api/users/sign-up")) {
			return chain.filter(exchange);
		}

		String accessToken = getAccessTokenFromHeader(exchange);
		JwtValidType resultJwtType = jwtUtil.validateToken(accessToken);
		if (resultJwtType != JwtValidType.VALID_TOKEN) {
			sendErrorResponse(exchange, resultJwtType);
			return exchange.getResponse().setComplete();
		}

		if (!jwtUtil.isAccessToken(accessToken)) {
			sendErrorResponse(exchange, JwtValidType.INVALID_TOKEN_TYPE);
			return exchange.getResponse().setComplete();
		}

		Claims claims = jwtUtil.getClaims(accessToken);
		String loginId = String.valueOf(claims.get(CLAIM_LOGIN_ID));
		String role = String.valueOf(claims.get(CLAIM_USER_ROLE));

		ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
			.header("X-Login-Id", loginId)
			.header("X-User-Role", role)
			.header("X-Token", accessToken)
			.build();

		log.info("Gateway User loginId : {} , role : {}", loginId, role);

		exchange = exchange.mutate().request(modifiedRequest).build();

		exchange.getRequest().getHeaders().forEach((name, values) -> {
			log.debug("Header: {} = {}", name, values);
		});

		return chain.filter(exchange);
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
