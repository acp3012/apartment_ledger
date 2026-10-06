package com.yesh.apartmentledger.finance.expense.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseDraftRequest(
        Long makerId,  // userid
        Long ledgerCategoryId,
        LocalDate transactionDate,
        BigDecimal amount,
        Long paymentModeId,
        String referenceNumber,
        String remarks
) {}
