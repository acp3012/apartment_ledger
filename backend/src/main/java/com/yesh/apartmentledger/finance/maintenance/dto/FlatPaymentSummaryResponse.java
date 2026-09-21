package com.yesh.apartmentledger.finance.maintenance.dto;

import java.math.BigDecimal;

public record FlatPaymentSummaryResponse(
        Long flatId,
        String flatNumber,
        BigDecimal monthlyFee,
        BigDecimal amountAlreadyPaidThisMonth,
        BigDecimal availableWalletBalance,
        BigDecimal netDue
) {}