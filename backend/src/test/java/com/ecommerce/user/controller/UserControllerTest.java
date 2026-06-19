package com.ecommerce.user.controller;

import com.ecommerce.common.exception.GlobalExceptionHandler;
import com.ecommerce.user.dto.UserResponse;
import com.ecommerce.user.entity.Role;
import com.ecommerce.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    private UserResponse userResponse() {
        return new UserResponse(
                1L,
                "Dharani",
                "dharani@example.com",
                Role.CUSTOMER,
                OffsetDateTime.parse("2026-06-19T10:00:00+05:30")
        );
    }
}
