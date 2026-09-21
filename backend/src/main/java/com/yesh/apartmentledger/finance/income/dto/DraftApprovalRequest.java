package com.yesh.apartmentledger.finance.income.dto;

import java.util.List;

public record DraftApprovalRequest(
        List<Long> draftIds
) {
}
