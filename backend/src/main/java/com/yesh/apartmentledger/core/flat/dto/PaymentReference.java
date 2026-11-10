package com.yesh.apartmentledger.core.flat.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentReference(
        Long paymentId,
        LocalDate transactionDate,
        String paymentMode,
        String referenceNumber,
        String remarks,
        BigDecimal amount
){}
