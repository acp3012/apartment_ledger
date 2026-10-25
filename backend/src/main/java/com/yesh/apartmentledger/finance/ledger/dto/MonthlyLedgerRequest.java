package com.yesh.apartmentledger.finance.ledger.dto;

import com.yesh.apartmentledger.core.validation.ValidMonth;
import java.math.BigDecimal;
import jakarta.validation.constraints.NotNull;

public record MonthlyLedgerRequest(
        Short year,
        @NotNull @ValidMonth Short month,
        BigDecimal openingBalance,
        BigDecimal income,
        BigDecimal expense
) {}
