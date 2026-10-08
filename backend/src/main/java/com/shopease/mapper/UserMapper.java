package com.shopease.mapper;

import com.shopease.dto.response.UserResponse;
import com.shopease.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User u) {
        return new UserResponse(u.getId(), u.getFullName(), u.getEmail(), u.getPhone(),
                u.getRole().getName().name(), u.isEnabled(), u.getCreatedAt());
    }
}
