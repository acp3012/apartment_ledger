package com.yesh.apartmentledger.finance.income.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record IncomeResponse(
        Long incomeId,
        LocalDate transactionDate,
        String apartmentName,
        String flatName,
        String flatOwnerName,
        String ledgerCategoryName ,
        String ledgerCategoryCode,
        String referenceNumber,
        String remarks,
        BigDecimal amount,
        String paymentModeName

) {
}
