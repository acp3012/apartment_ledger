package com.yesh.apartmentledger.finance.expense.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ExpenseApprovalRequest(
        Long userId,
        List<Long> draftIds,
        @Size(max = 200, message = "Approval comments cannot exceed 200 characters")
        @Nullable String rejectReason

) {}

