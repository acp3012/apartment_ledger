package com.yesh.apartmentledger.auth.controller;

import com.yesh.apartmentledger.auth.dto.*;
import com.yesh.apartmentledger.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "User management", description = "Provides APIs for creating application user and login.")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // First time admin user login
    @PostMapping("admin-login")
    @Operation (summary = "First time Admin login", description = "Check if the user is a first time admin user.")
    public ResponseEntity<AuthResponse> adminLogin(@RequestBody AdminLoginRequest request) {
        return ResponseEntity.ok(authService.firstTimeAdminLogin(request.emailId()));
    }

    @PostMapping("admin-password")
    @Operation (summary = "Create password", description = "Create password for first time admin user.")
    public ResponseEntity<?> createAdminPassword(@RequestBody CreatePasswordRequest request) {
       
        authService.createAdminPassword(request);
        return ResponseEntity.created(null).build();
    }
    

    @PostMapping("/register")
    @Operation(summary = "Register a first time user", description = "First time user creation with credentials")
    public ResponseEntity<?> register(
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

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Reset password for a user.")
    public ResponseEntity<?> resetPassword(
           @Valid @RequestBody PasswordChangeRequest passwordChangRequest){

        authService.resetPassword(passwordChangRequest);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/hash-password")
    @Operation (summary = "Hash password", description = "Hash a password using the application's hashing algorithm.")
    public ResponseEntity<String> hashPassword(
            @Parameter(description = "Password to be hashed")
            @RequestParam String password) {
        String hashedPassword = authService.hashPassword(password);
        return ResponseEntity.ok(hashedPassword);
    }
  
}