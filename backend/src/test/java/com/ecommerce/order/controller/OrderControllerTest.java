package com.ecommerce.order.controller;

import com.ecommerce.common.exception.GlobalExceptionHandler;
import com.ecommerce.order.dto.OrderItemResponse;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.entity.OrderStatus;
import com.ecommerce.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class OrderControllerTest {

    private static final String EMAIL = "dharani@example.com";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Test
    void createOrderShouldReturnCreatedOrder() throws Exception {
        when(orderService.createOrder(eq(EMAIL), any())).thenReturn(orderResponse());

        mockMvc.perform(post("/api/orders")
                        .principal(authentication())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateOrderJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.customerName").value("Dharani"))
                .andExpect(jsonPath("$.customerEmail").value(EMAIL))
                .andExpect(jsonPath("$.shippingCity").value("Chennai"))
                .andExpect(jsonPath("$.totalItems").value(2))
                .andExpect(jsonPath("$.totalAmount").value(259.98));
    }

    @Test
    void createOrderShouldRejectMissingAddressFields() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .principal(authentication())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "shippingCity": "Chennai"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.shippingAddressLine").value("Shipping address line is required"))
                .andExpect(jsonPath("$.validationErrors.shippingState").value("Shipping state is required"))
                .andExpect(jsonPath("$.validationErrors.shippingPostalCode").value("Shipping postal code is required"))
                .andExpect(jsonPath("$.validationErrors.shippingCountry").value("Shipping country is required"));
    }

    @Test
    void getCurrentUserOrdersShouldReturnOrders() throws Exception {
        when(orderService.findCurrentUserOrders(EMAIL)).thenReturn(List.of(orderResponse()));

        mockMvc.perform(get("/api/orders").principal(authentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].customerEmail").value(EMAIL))
                .andExpect(jsonPath("$[0].shippingAddressLine").value("123 Main Street"))
                .andExpect(jsonPath("$[0].items[0].productName").value("Keyboard"));
    }

    @Test
    void getCurrentUserOrderShouldReturnOrder() throws Exception {
        when(orderService.findCurrentUserOrderById(EMAIL, 1L)).thenReturn(orderResponse());

        mockMvc.perform(get("/api/orders/{id}", 1L).principal(authentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    private String validCreateOrderJson() {
        return """
                {
                  "shippingAddressLine": "123 Main Street",
                  "shippingCity": "Chennai",
                  "shippingState": "Tamil Nadu",
                  "shippingPostalCode": "600001",
                  "shippingCountry": "India"
                }
                """;
    }

    private UsernamePasswordAuthenticationToken authentication() {
        return new UsernamePasswordAuthenticationToken(EMAIL, null);
    }

    private OrderResponse orderResponse() {
        OffsetDateTime now = OffsetDateTime.parse("2026-06-21T10:00:00+05:30");
        return new OrderResponse(
                1L,
                OrderStatus.PENDING,
                "Dharani",
                EMAIL,
                "123 Main Street",
                "Chennai",
                "Tamil Nadu",
                "600001",
                "India",
                List.of(new OrderItemResponse(
                        10L,
                        1L,
                        "Keyboard",
                        new BigDecimal("129.99"),
                        2,
                        new BigDecimal("259.98")
                )),
                2,
                new BigDecimal("259.98"),
                now,
                now
        );
    }
}
