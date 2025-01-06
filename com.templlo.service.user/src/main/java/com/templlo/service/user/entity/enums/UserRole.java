package com.templlo.service.user.entity.enums;

public enum UserRole {
	MEMBER("MEMBER"),
	TEMPLE_ADMIN("TEMPLE_ADMIN"),
	MASTER("MASTER");

	private final String authority;

	UserRole(String authority) {
		this.authority = authority;
	}

	public String getAuthority() {
		return this.authority;
	}

	public static UserRole fromString(String authority) {
		return UserRole.valueOf(authority);
	}
}
