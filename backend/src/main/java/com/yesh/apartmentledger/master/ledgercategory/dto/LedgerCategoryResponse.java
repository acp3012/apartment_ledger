package com.yesh.apartmentledger.master.ledgercategory.dto;

public record LedgerCategoryResponse(
        Long id,
        String categoryName,
        String transactionType,
        Boolean isFlatMaintenance,
        Boolean isActive,
        Integer displayOrder
) {}
