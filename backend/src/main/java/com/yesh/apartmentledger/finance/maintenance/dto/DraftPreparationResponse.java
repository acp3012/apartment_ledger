package com.yesh.apartmentledger.finance.maintenance.dto;

import java.math.BigDecimal;

public record DraftPreparationResponse(
        Long flatId,
        String flatNumber,

        // The active rate fetched from maintenance_rate
        BigDecimal monthlyFee,

        // The pre-calculated suggestion for the input box: Math.min(fee, totalAvailableAdvance)
        BigDecimal suggestedAdvanceDeduction,

        // Total advance available to display in the UI label
        BigDecimal totalAvailableAdvance,

        // What they still owe (monthlyFee - suggestedAdvanceDeduction)
        BigDecimal netDueOutOfPocket,

        // Allows the UI to disable the row and show an error if a rate is missing
        boolean isRateDefined,
        String rateErrorMessage
) {}