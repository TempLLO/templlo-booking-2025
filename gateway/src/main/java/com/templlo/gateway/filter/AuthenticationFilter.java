package com.templlo.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;

import com.templlo.gateway.util.JwtUtil;
import com.templlo.gateway.util.JwtValidType;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j(topic = " == Gateway: Authentication Filter == ")
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
		if (jwtUtil.validateToken(accessToken) != JwtValidType.VALID_TOKEN) {
			exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
			return exchange.getResponse().setComplete();
		}

		if(!jwtUtil.isAccessToken(accessToken)) {
			log.error("[ERROR] Invalid Token Type");
			exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
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

	private String getAccessTokenFromHeader(ServerWebExchange exchange) {
		String bearerToken = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
			return bearerToken.substring(BEARER_PREFIX.length());
		}
		return null;
	}

}
