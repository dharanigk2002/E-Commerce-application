package com.ecommerce.cart.controller;

import com.ecommerce.cart.dto.AddCartItemRequest;
import com.ecommerce.cart.dto.CartItemResponse;
import com.ecommerce.cart.dto.CartResponse;
import com.ecommerce.cart.dto.UpdateCartItemRequest;
import com.ecommerce.cart.service.CartService;
import com.ecommerce.common.exception.GlobalExceptionHandler;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CartController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class CartControllerTest {

    private static final String EMAIL = "dharani@example.com";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartService cartService;

    @Test
    void getCartShouldReturnCurrentUsersCart() throws Exception {
        when(cartService.getCart(EMAIL)).thenReturn(cartResponse());

        mockMvc.perform(get("/api/cart").principal(authentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.totalItems").value(2))
                .andExpect(jsonPath("$.totalAmount").value(259.98))
                .andExpect(jsonPath("$.items[0].productName").value("Keyboard"));
    }

    @Test
    void addItemShouldReturnCreatedCart() throws Exception {
        when(cartService.addItem(eq(EMAIL), any(AddCartItemRequest.class))).thenReturn(cartResponse());

        mockMvc.perform(post("/api/cart/items")
                        .principal(authentication())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addItemJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalItems").value(2));
    }

    @Test
    void addItemShouldReturnBadRequestForInvalidQuantity() throws Exception {
        String invalidJson = """
                {
                  "productId": 1,
                  "quantity": 0
                }
                """;

        mockMvc.perform(post("/api/cart/items")
                        .principal(authentication())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.quantity").value("Quantity must be at least 1"));
    }

    @Test
    void updateItemShouldReturnUpdatedCart() throws Exception {
        when(cartService.updateItem(eq(EMAIL), eq(10L), any(UpdateCartItemRequest.class))).thenReturn(cartResponse());

        mockMvc.perform(put("/api/cart/items/{itemId}", 10L)
                        .principal(authentication())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateItemJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(2));
    }

    @Test
    void removeItemShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/cart/items/{itemId}", 10L).principal(authentication()))
                .andExpect(status().isNoContent());

        verify(cartService).removeItem(EMAIL, 10L);
    }

    @Test
    void clearCartShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/cart").principal(authentication()))
                .andExpect(status().isNoContent());

        verify(cartService).clearCart(EMAIL);
    }

    private UsernamePasswordAuthenticationToken authentication() {
        return new UsernamePasswordAuthenticationToken(EMAIL, null);
    }

    private String addItemJson() {
        return """
                {
                  "productId": 1,
                  "quantity": 2
                }
                """;
    }

    private String updateItemJson() {
        return """
                {
                  "quantity": 2
                }
                """;
    }

    private CartResponse cartResponse() {
        return new CartResponse(
                1L,
                List.of(new CartItemResponse(
                        10L,
                        1L,
                        "Keyboard",
                        new BigDecimal("129.99"),
                        2,
                        new BigDecimal("259.98")
                )),
                2,
                new BigDecimal("259.98")
        );
    }
}
