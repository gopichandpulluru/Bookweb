package com.shopease.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

public final class PageUtil {

    private PageUtil() {
    }

    /**
     * Builds a safe Pageable from raw query params.
     * The sort field is checked against a whitelist so clients can't sort by arbitrary columns.
     */
    public static Pageable of(int page, int size, String sortBy, String direction,
                              Set<String> allowedSortFields, String defaultSortField) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), Constants.MAX_PAGE_SIZE);
        String field = allowedSortFields.contains(sortBy) ? sortBy : defaultSortField;
        Sort.Direction dir = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(safePage, safeSize, Sort.by(dir, field));
    }
}
