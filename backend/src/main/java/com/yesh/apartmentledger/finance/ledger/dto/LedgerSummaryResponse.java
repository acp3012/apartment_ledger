package com.yesh.apartmentledger.finance.ledger.dto;

import java.math.BigDecimal;

public record LedgerSummaryResponse (
        Long apartmentId,
        Short year,
        Short month,
        BigDecimal openingBalance,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal closingBalance

){

}
