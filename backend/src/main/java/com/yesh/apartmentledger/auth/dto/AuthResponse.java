package com.yesh.apartmentledger.auth.dto;

public record AuthResponse(
        Long userId,
        String token,
        String displayName,
        String email,
        Long apartmentId,
        String apartmentName,
        Long flatId,
        String flatNumber,
        boolean isAdmin,
        String message
) {}