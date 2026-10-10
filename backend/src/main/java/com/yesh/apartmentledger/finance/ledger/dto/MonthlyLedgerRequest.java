package com.yesh.apartmentledger.finance.ledger.dto;

import java.math.BigDecimal;

public record MonthlyLedgerRequest(
        Short year,
        Short month,
        BigDecimal openingBalance,
        BigDecimal income,
        BigDecimal expense
) {}
