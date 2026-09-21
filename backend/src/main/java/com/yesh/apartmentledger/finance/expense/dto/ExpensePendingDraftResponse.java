package com.yesh.apartmentledger.finance.expense.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ExpensePendingDraftResponse(
        Long draftId,
        String categoryName,
        LocalDate transactionDate,
        Short txnYear,
        Short txnMonth,
        BigDecimal amount,
        String paymentMode,
        String referenceNumber,
        String remarks,
        String makerName
) {}

