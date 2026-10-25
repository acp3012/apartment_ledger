package com.yesh.apartmentledger.auth.dto;

import com.yesh.apartmentledger.master.role.entity.Role;

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
        boolean isAdmin,
        List<String> roleNames,
        String message
) {}