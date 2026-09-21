package com.yesh.apartmentledger.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Email Id is required")
        String email,

        @NotBlank(message = "Password is required")
        String password
) {}