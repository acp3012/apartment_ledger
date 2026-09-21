package com.yesh.apartmentledger.core.flat.dto;

public record FlatResponse (
        Long flatId,
        String flatNumber,
        String ownerName
) {}
