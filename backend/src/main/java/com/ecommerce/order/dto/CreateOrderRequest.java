package com.ecommerce.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateOrderRequest(
        @NotBlank(message = "Shipping address line is required")
        @Size(max = 255, message = "Shipping address line must be at most 255 characters")
        String shippingAddressLine,

        @NotBlank(message = "Shipping city is required")
        @Size(max = 120, message = "Shipping city must be at most 120 characters")
        String shippingCity,

        @NotBlank(message = "Shipping state is required")
        @Size(max = 120, message = "Shipping state must be at most 120 characters")
        String shippingState,

        @NotBlank(message = "Shipping postal code is required")
        @Size(max = 30, message = "Shipping postal code must be at most 30 characters")
        String shippingPostalCode,

        @NotBlank(message = "Shipping country is required")
        @Size(max = 120, message = "Shipping country must be at most 120 characters")
        String shippingCountry
) {
}
