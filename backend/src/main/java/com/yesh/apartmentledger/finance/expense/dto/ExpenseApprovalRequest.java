package com.yesh.apartmentledger.finance.expense.dto;

import com.yesh.apartmentledger.core.user.entity.AppUser;

import java.util.List;

public record ExpenseApprovalRequest(
        Long userId,
        List<Long> draftIds
) {}

