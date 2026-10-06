package com.yesh.apartmentledger.core.flat.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FlatPaymentStatusResponse(
        String flatNumber,
        String ownerName,
        Short year,
        Short month,
        String status,
        BigDecimal amountPaid,
        LocalDate lastPaymentDate,
        String paymentModeName,
        String referenceNumber,
        BigDecimal arrearsAmount
) {}
