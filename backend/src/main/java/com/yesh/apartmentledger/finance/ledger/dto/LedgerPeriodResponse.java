package com.yesh.apartmentledger.finance.ledger.dto;

public record LedgerPeriodResponse(
        Long apartmentId,
        Short year,
        Short month,
        String monthName
) {}

