package com.templlo.service.review.common.audit;

import java.util.Optional;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.templlo.service.review.common.security.UserDetailsImpl;

public class AuditorAwareImpl implements AuditorAware<String> {

	private static final String ANONYMOUS_USER = "anonymousUser";

	@Override
	public Optional<String> getCurrentAuditor() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			return Optional.empty();
		}

		if (ANONYMOUS_USER.equals(authentication.getName())) {
			return Optional.of("anonymousUser");
		}

		Object principal = authentication.getPrincipal();
		if (principal instanceof UserDetailsImpl) {
			UserDetailsImpl userDetails = (UserDetailsImpl) principal;
			return Optional.of((userDetails.getLoginId()));
		}

		return Optional.empty();
	}
}
