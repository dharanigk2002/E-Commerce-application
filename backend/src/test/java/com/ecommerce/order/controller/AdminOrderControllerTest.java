package com.ecommerce.order.controller;

import com.ecommerce.common.exception.GlobalExceptionHandler;
import com.ecommerce.order.dto.OrderItemResponse;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.dto.UpdateOrderStatusRequest;
import com.ecommerce.order.entity.OrderStatus;
import com.ecommerce.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminOrderController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AdminOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Test
    void getOrdersShouldReturnAllOrders() throws Exception {
        when(orderService.findAllOrders()).thenReturn(List.of(orderResponse(OrderStatus.PENDING)));

        mockMvc.perform(get("/api/admin/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void updateOrderStatusShouldReturnUpdatedOrder() throws Exception {
        when(orderService.updateStatus(eq(1L), any(UpdateOrderStatusRequest.class)))
                .thenReturn(orderResponse(OrderStatus.CONFIRMED));

        mockMvc.perform(put("/api/admin/orders/{id}/status", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "CONFIRMED"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void updateOrderStatusShouldReturnBadRequestWhenStatusIsMissing() throws Exception {
        mockMvc.perform(put("/api/admin/orders/{id}/status", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.status").value("Order status is required"));
    }

    private OrderResponse orderResponse(OrderStatus status) {
        OffsetDateTime now = OffsetDateTime.parse("2026-06-21T10:00:00+05:30");
        return new OrderResponse(
                1L,
                status,
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
