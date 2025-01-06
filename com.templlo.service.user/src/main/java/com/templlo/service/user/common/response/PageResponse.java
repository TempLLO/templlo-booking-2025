package com.templlo.service.user.common.response;

import java.util.List;

import org.springframework.data.domain.Page;

public record PageResponse<T>(
	int totalPages,
	int pageNumber,
	List<T> content
) {
	public static <T> PageResponse<T> of(Page<T> page) {
		return new PageResponse<>(
			page.getTotalPages(),
			page.getNumber() + 1,
			page.getContent()
		);
	}
}
