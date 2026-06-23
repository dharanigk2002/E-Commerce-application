package com.ecommerce.order.dto;

import com.ecommerce.order.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        OrderStatus status,
        String customerName,
        String customerEmail,
        String shippingAddressLine,
        String shippingCity,
        String shippingState,
        String shippingPostalCode,
        String shippingCountry,
        List<OrderItemResponse> items,
        Integer totalItems,
        BigDecimal totalAmount,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
