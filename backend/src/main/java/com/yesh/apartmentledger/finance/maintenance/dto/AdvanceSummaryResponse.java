package com.yesh.apartmentledger.finance.maintenance.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AdvanceSummaryResponse (
        Long id,
        String flatNumber,
        LocalDate receiptDate,
        BigDecimal advanceAmount,
        BigDecimal remainingBalance,
        String paymentMode
) {}
