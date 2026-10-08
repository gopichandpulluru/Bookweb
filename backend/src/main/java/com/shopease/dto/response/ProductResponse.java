package com.shopease.dto.response;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String name,
        String slug,
        String description,
        BigDecimal price,
        int stock,
        String imageUrl,
        boolean active,
        Long categoryId,
        String categoryName
) {
}
