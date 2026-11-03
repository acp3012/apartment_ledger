package com.yesh.apartmentledger.auth.dto;

import java.util.List;

public record AuthResponse(
        Long userId,
        String token,
        String displayName,
        String email,
        Long apartmentId,
        String apartmentName,
        Long flatId,
        String flatNumber,
        String ownerName,
        boolean isAdmin,
        boolean isSetupCompleted,
        List<String> roleNames,
        String message
) {}