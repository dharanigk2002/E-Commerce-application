package com.ecommerce.product.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        Integer availableStock,
        Boolean active,
        String imageUrl,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
