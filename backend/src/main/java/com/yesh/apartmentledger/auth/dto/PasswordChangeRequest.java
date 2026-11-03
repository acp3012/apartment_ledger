package com.yesh.apartmentledger.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;

public record PasswordChangeRequest(
        @NotBlank(message = "Email Id is required")

        String emailId,
        @NotBlank(message = "Old password is required.")
        String oldPassword,
        @NotBlank(message = "Password cannot be blank.")
        @Size(min=6, message = "Password length must be minimum 6.")
        String newPassword

) {
}
