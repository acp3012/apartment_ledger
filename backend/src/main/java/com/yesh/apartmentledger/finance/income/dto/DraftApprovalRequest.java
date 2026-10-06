package com.yesh.apartmentledger.finance.income.dto;

import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

import java.util.List;

public record DraftApprovalRequest(
        List<Long> draftIds,
        Long approverId, // userid
        @Size(max = 200, message = "Approval comments cannot exceed 200 characters")
        @Nullable String comments )
{
}
