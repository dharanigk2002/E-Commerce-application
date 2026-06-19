package com.ecommerce.auth.dto;

import com.ecommerce.user.dto.UserResponse;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        UserResponse user
) {
}
