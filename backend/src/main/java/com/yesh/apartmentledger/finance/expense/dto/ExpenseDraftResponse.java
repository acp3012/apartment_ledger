package com.yesh.apartmentledger.finance.expense.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ExpenseDraftResponse(
        Long id,
        LocalDate transactionDate,
        String ledgerCategoryName,
        BigDecimal amount,
        String paymentModeName,
        String referenceNumber,
        String remarks,
        String approvalStatus,
        LocalDateTime createdDate,
        String createdBy,
        LocalDateTime approvedDate,
        String approvedBy
        ) {}
