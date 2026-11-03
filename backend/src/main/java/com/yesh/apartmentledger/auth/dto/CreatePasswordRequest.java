package com.yesh.apartmentledger.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record CreatePasswordRequest(
    @NotBlank (message = "Email Id is required")
    String emailId,
    @NotBlank (message = "Password cannot be blank.")
    @jakarta.validation.constraints.Size(min=6, message = "Password length must be minimum 6.")
    String newPassword
) {}