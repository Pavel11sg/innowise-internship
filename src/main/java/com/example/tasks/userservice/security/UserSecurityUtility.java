package com.example.tasks.userservice.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserSecurityUtility {

	public UUID getCurrentUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.getPrincipal() instanceof UUID) {
			return (UUID) authentication.getPrincipal();
		}
		throw new SecurityException("User not authenticated or invalid principal type");
	}

	public boolean isAdmin() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return authentication != null &&
				authentication.getAuthorities().stream()
						.anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
	}
}