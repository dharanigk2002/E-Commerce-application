package com.ecommerce.user.controller;

import com.ecommerce.common.exception.GlobalExceptionHandler;
import com.ecommerce.user.dto.UpdateAddressRequest;
import com.ecommerce.user.dto.UserResponse;
import com.ecommerce.user.entity.Role;
import com.ecommerce.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void getCurrentUserShouldReturnAuthenticatedUser() throws Exception {
        String email = "dharani@example.com";
        when(userService.findByEmail(email)).thenReturn(userResponse());

        mockMvc.perform(get("/api/users/me")
                        .principal(new UsernamePasswordAuthenticationToken(email, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fullName").value("Dharani"))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.shippingCity").value("Chennai"));
    }

    @Test
    void updateCurrentUserAddressShouldReturnUpdatedUser() throws Exception {
        String email = "dharani@example.com";
        when(userService.updateAddress(eq(email), any(UpdateAddressRequest.class))).thenReturn(userResponse());

        mockMvc.perform(put("/api/users/me/address")
                        .principal(new UsernamePasswordAuthenticationToken(email, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addressJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shippingAddressLine").value("123 Main Street"))
                .andExpect(jsonPath("$.shippingCity").value("Chennai"))
                .andExpect(jsonPath("$.shippingCountry").value("India"));
    }

    @Test
    void updateCurrentUserAddressShouldRejectMissingFields() throws Exception {
        String email = "dharani@example.com";

        mockMvc.perform(put("/api/users/me/address")
                        .principal(new UsernamePasswordAuthenticationToken(email, null))
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

    private String addressJson() {
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

    private UserResponse userResponse() {
        return new UserResponse(
                1L,
                "Dharani",
                "dharani@example.com",
                Role.CUSTOMER,
                "123 Main Street",
                "Chennai",
                "Tamil Nadu",
                "600001",
                "India",
                OffsetDateTime.parse("2026-06-19T10:00:00+05:30")
        );
    }
}
