package com.yesh.apartmentledger.master.paymentmode.dto;

public record PaymentModeResponse(
        Long id,
        String name,
        Boolean isActive,
        Integer displayOrder
) {}
