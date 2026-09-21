package com.yesh.apartmentledger.finance.income.dto;

import jakarta.validation.constraints.Size;

public record IncomeApprovalRequest(

        @Size(max = 500, message = "Approval comments cannot exceed 500 characters")
        String comments
) {}