package com.shopease.dto.response;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String fullName,
        String email,
        String phone,
        String role,
        boolean enabled,
        LocalDateTime createdAt
) {
}
