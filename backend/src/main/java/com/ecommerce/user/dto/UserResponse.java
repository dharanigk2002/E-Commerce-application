package com.ecommerce.user.dto;

import com.ecommerce.user.entity.Role;

import java.time.OffsetDateTime;

public record UserResponse(
        Long id,
        String fullName,
        String email,
        Role role,
        OffsetDateTime createdAt
) {
}
