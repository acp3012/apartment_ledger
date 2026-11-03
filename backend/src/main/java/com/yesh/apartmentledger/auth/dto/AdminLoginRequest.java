package com.yesh.apartmentledger.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record AdminLoginRequest(
        @NotBlank(message = "Email Id is required")
        String emailId
) {}
