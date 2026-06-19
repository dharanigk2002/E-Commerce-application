package com.ecommerce.auth.service;

import com.ecommerce.auth.dto.LoginRequest;
import com.ecommerce.auth.dto.RegisterRequest;
import com.ecommerce.common.exception.DuplicateResourceException;
import com.ecommerce.common.exception.InvalidCredentialsException;
import com.ecommerce.user.entity.Role;
import com.ecommerce.user.entity.User;
import com.ecommerce.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerShouldCreateCustomerWithEncodedPasswordAndReturnToken() {
        RegisterRequest request = new RegisterRequest(
                "Dharani",
                "Dharani@Example.com",
                "password123"
        );

        when(userRepository.existsByEmail("dharani@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");
        when(jwtService.getExpirationSeconds()).thenReturn(3600L);

        var response = authService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getFullName()).isEqualTo("Dharani");
        assertThat(savedUser.getEmail()).isEqualTo("dharani@example.com");
        assertThat(savedUser.getPasswordHash()).isEqualTo("encoded-password");
        assertThat(savedUser.getPasswordHash()).isNotEqualTo("password123");
        assertThat(savedUser.getRole()).isEqualTo(Role.CUSTOMER);

        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresInSeconds()).isEqualTo(3600L);
        assertThat(response.user().email()).isEqualTo("dharani@example.com");
        assertThat(response.user().role()).isEqualTo(Role.CUSTOMER);
    }

    @Test
    void registerShouldThrowWhenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest(
                "Dharani",
                "dharani@example.com",
                "password123"
        );

        when(userRepository.existsByEmail("dharani@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Email is already registered");
    }

    @Test
    void loginShouldReturnTokenWhenCredentialsAreValid() {
        LoginRequest request = new LoginRequest("dharani@example.com", "password123");
        User user = new User("Dharani", "dharani@example.com", "encoded-password", Role.CUSTOMER);

        when(userRepository.findByEmail("dharani@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encoded-password")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");
        when(jwtService.getExpirationSeconds()).thenReturn(3600L);

        var response = authService.login(request);

        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.user().email()).isEqualTo("dharani@example.com");
    }

    @Test
    void loginShouldThrowWhenPasswordIsWrong() {
        LoginRequest request = new LoginRequest("dharani@example.com", "wrong-password");
        User user = new User("Dharani", "dharani@example.com", "encoded-password", Role.CUSTOMER);

        when(userRepository.findByEmail("dharani@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    void loginShouldThrowWhenEmailDoesNotExist() {
        LoginRequest request = new LoginRequest("missing@example.com", "password123");

        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");
    }
}
