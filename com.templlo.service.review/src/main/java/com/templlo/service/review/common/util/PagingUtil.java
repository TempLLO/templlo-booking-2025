package com.templlo.service.review.common.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PagingUtil {

	public static Pageable of(int page, int size, String sortBy, boolean isAsc) {

		size = (size == 5 || size == 10) ? size : 10;

		Sort.Direction direction = isAsc ? Sort.Direction.ASC : Sort.Direction.DESC;
		Sort sort = Sort.by(direction, sortBy.equals("createdAt") ? "createdAt" : "rating");

		return PageRequest.of(page, size, sort);
	}

}
