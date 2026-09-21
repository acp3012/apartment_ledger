package com.yesh.apartmentledger.finance.maintenance.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AdvancePaymentRequest(
        Long flatId,
        LocalDate receiptDate,
        BigDecimal advanceAmount,
        Long paymentModeId,
        String referenceNumber
) {}