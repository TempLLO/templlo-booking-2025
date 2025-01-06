package com.templlo.service.user.common.audit;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.templlo.service.user.common.security.UserDetailsImpl;

public class AuditorAwareImpl implements AuditorAware<UUID> {

	private static final String ANONYMOUS_USER = "anonymousUser";

	@Override
	public Optional<UUID> getCurrentAuditor() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			return Optional.empty();
		}

		if (ANONYMOUS_USER.equals(authentication.getName())) {
			return Optional.empty();
		}

		Object principal = authentication.getPrincipal();
		if (principal instanceof UserDetailsImpl) {
			UserDetailsImpl userDetails = (UserDetailsImpl) principal;
			return Optional.of((userDetails.getUser().getId()));
		}

		return Optional.empty();
	}
}
