package com.yesh.apartmentledger.core.flat.dto;

public record FlatResponse (
        Long apartmentId,
        Long flatId,
        Short floorNumber,
        String flatNumber,
        String ownerName,
        String ownerMobile,
        String ownerEmail
) {}
