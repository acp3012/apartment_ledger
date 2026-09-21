package com.yesh.apartmentledger.finance.income.dto;

import com.yesh.apartmentledger.master.ledgercategory.entity.LedgerCategory;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;


public record IncomeDraftRequest(

        // Optional: Only provided if the income is tied to a specific flat (like Maintenance)
        Long flatId,
        @NotNull(message = "Ledger category is required")
        Long ledgerCategoryId,

        @NotNull(message = "Transaction date is required")
        LocalDate transactionDate,

        Short txnMonth,

        Short txnYear ,
        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be strictly greater than zero")
        BigDecimal amount,

        @NotNull(message = "Payment mode is required")
        Long paymentModeId,

        @Size(max = 50, message = "Reference number cannot exceed 50 characters")
        String referenceNumber,

        @Size(max = 500, message = "Remarks cannot exceed 500 characters")
        String remarks,
        @NotNull(message = "The user id cannot be null")
        Long makerId

) {}