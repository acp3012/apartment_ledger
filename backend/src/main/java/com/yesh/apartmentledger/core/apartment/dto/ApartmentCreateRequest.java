package com.yesh.apartmentledger.core.apartment.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ApartmentCreateRequest(
        @NotBlank @Size(max = 20) String apartmentCode,
        @NotBlank @Size(max = 150) String apartmentName,
        @Size(max = 200) String address,
        @Size(max = 100) String city,
        @Size(max = 100) String state,
        @Size(max = 10) String pincode,
        @Size(max = 100) String bankName,
        @Size(max = 30) String accountNumber,
        @Size(max = 20) String ifscCode,
        Short goLiveYear,
        @Min(1) @Max(12) Short goLiveMonth
) {
}