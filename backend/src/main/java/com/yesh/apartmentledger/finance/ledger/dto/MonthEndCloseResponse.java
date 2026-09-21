package com.yesh.apartmentledger.finance.ledger.dto;

import java.math.BigDecimal;

public record MonthEndCloseResponse(
        Long apartmentId,
        Short year,
        Short month,
        BigDecimal openingBalance,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal computedClosingBalance,
        boolean isAlreadyClosed
) {}