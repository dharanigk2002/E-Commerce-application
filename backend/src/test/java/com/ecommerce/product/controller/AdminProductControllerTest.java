package com.ecommerce.product.controller;

import com.ecommerce.common.exception.GlobalExceptionHandler;
import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.product.dto.ProductRequest;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminProductController.class)
@Import(GlobalExceptionHandler.class)
class AdminProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Test
    void createProductShouldReturnCreatedProduct() throws Exception {
        when(productService.create(any(ProductRequest.class))).thenReturn(productResponse());

        mockMvc.perform(post("/api/admin/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validProductJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Keyboard"))
                .andExpect(jsonPath("$.price").value(129.99));
    }

    @Test
    void createProductShouldReturnBadRequestForInvalidBody() throws Exception {
        String invalidJson = """
                {
                  "name": "",
                  "description": "Invalid product",
                  "price": -10,
                  "availableStock": -1,
                  "active": true
                }
                """;

        mockMvc.perform(post("/api/admin/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.validationErrors.name").value("Product name is required"))
                .andExpect(jsonPath("$.validationErrors.price").value("Price must be greater than zero"))
                .andExpect(jsonPath("$.validationErrors.availableStock").value("Available stock cannot be negative"));
    }

    @Test
    void updateProductShouldReturnUpdatedProduct() throws Exception {
        when(productService.update(eq(1L), any(ProductRequest.class))).thenReturn(productResponse());

        mockMvc.perform(put("/api/admin/products/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validProductJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Keyboard"));
    }

    @Test
    void updateProductShouldReturnNotFoundWhenMissing() throws Exception {
        when(productService.update(eq(99L), any(ProductRequest.class)))
                .thenThrow(new ResourceNotFoundException("Product not found with id: 99"));

        mockMvc.perform(put("/api/admin/products/{id}", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validProductJson()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Product not found with id: 99"));
    }

    @Test
    void deleteProductShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/admin/products/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(productService).delete(1L);
    }

    @Test
    void deleteProductShouldReturnNotFoundWhenMissing() throws Exception {
        doThrow(new ResourceNotFoundException("Product not found with id: 99"))
                .when(productService).delete(99L);

        mockMvc.perform(delete("/api/admin/products/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Product not found with id: 99"));
    }

    private String validProductJson() {
        return """
                {
                  "name": "Keyboard",
                  "description": "Mechanical keyboard",
                  "price": 129.99,
                  "availableStock": 10,
                  "active": true
                }
                """;
    }

    private ProductResponse productResponse() {
        OffsetDateTime now = OffsetDateTime.parse("2026-06-18T10:00:00+05:30");
        return new ProductResponse(
                1L,
                "Keyboard",
                "Mechanical keyboard",
                new BigDecimal("129.99"),
                10,
                true,
                now,
                now
        );
    }
}
