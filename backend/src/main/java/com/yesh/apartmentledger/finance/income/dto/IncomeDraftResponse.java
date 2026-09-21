package com.yesh.apartmentledger.finance.income.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record IncomeDraftResponse(
        Long draftId,
        Long flatId,
        String flatNumber,
        LocalDate transactionDate,
        BigDecimal amount,
        Long paymentModeId,
        String paymentModeName,
        String referenceNumber,
        String remarks,
        String approvalStatus,
        String makerName,
        Long ledgerCategoryId
) {}