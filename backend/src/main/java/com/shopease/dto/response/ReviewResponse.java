package com.shopease.dto.response;

import java.time.LocalDateTime;

public record ReviewResponse(
        Long id,
        Long productId,
        String reviewerName,
        int rating,
        String comment,
        LocalDateTime createdAt
) {
}
