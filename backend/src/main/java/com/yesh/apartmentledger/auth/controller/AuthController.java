package com.yesh.apartmentledger.auth.controller;

import com.yesh.apartmentledger.auth.dto.AuthResponse;
import com.yesh.apartmentledger.auth.dto.LoginRequest;
import com.yesh.apartmentledger.auth.dto.RegisterRequest;
import com.yesh.apartmentledger.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "User management", description = "Provides APIs for creating application user and login.")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a first time user", description = "First time user creation with credentials")
    public ResponseEntity register(
            @Parameter(description = "Request payload for registration.")
            @Valid @RequestBody
            RegisterRequest request
    ) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "User authentication API.")
    public ResponseEntity<AuthResponse> login(
            @Parameter(description = "User login request")
            @Valid @RequestBody
            LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}