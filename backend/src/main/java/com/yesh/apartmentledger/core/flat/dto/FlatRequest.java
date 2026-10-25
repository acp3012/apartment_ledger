package com.yesh.apartmentledger.core.flat.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FlatRequest(
        @Size (max = 10, message = "Flat number cannot exceed 10 characters")
        String flatNumber,
        @Size (max = 10, message = "Floor number cannot exceed 10 characters")
        Short floorNumber,
        
        @NotBlank (message = "Owner name cannot be blank")
        @Size(min=3 , max = 50, message = "Owner name cannot exceed 50 characters")
        String ownerName,
        
        @NotBlank (message = "Owner mobile number cannot be blank")
        @Size(min = 10, max = 20, message = "Owner mobile number must be between 10 and 20 characters")
        String ownerMobileNumber,
        @Email (message = "Owner email must be a valid email address")
        String ownerEmail,
        //optional fields
        BigDecimal maintenanceFee

) {
}
