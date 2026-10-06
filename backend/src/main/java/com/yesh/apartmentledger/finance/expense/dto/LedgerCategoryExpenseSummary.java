package com.yesh.apartmentledger.finance.expense.dto;

import java.math.BigDecimal;

public record LedgerCategoryExpenseSummary(
        String ledgerCategoryName, BigDecimal expenseAmount, Long transactionCount,
        Short year, Short month, long apartmentId
) {}
