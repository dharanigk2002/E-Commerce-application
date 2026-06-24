package com.ecommerce.user.controller;

import com.ecommerce.user.dto.UpdateAddressRequest;
import com.ecommerce.user.dto.UserResponse;
import com.ecommerce.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        return ResponseEntity.ok(userService.findByEmail(authentication.getName()));
    }

    @PutMapping("/me/address")
    public ResponseEntity<UserResponse> updateCurrentUserAddress(
            Authentication authentication,
            @Valid @RequestBody UpdateAddressRequest request
    ) {
        return ResponseEntity.ok(userService.updateAddress(authentication.getName(), request));
    }
}
