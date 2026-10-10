package com.yesh.apartmentledger.core.apartment.dto;

import com.yesh.apartmentledger.core.apartment.entity.Apartment;

public record ApartmentResponse(
        Long apartmentId,
        String apartmentCode,
        String apartmentName,
        String address,
        String city,
        String state,
        String pincode,
        String bankName,
        String accountNumber,
        String ifscCode,
        Boolean isActive,
        Short goLiveYear,
        Short goLiveMonth
) {
    public static ApartmentResponse from(Apartment apartment) {
        return new ApartmentResponse(
                apartment.getId(),
                apartment.getApartmentCode(),
                apartment.getApartmentName(),
                apartment.getAddress(),
                apartment.getCity(),
                apartment.getState(),
                apartment.getPincode(),
                apartment.getBankName(),
                apartment.getAccountNumber(),
                apartment.getIfscCode(),
                apartment.getIsActive(),
                apartment.getGoLiveYear(),
                apartment.getGoLiveMonth());
    }
}