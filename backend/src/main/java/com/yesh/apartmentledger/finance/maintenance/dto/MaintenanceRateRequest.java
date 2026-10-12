package com.yesh.apartmentledger.finance.maintenance.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.yesh.apartmentledger.core.user.entity.AppUser;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

// Maintenance Rate at apartment level.
public record MaintenanceRateRequest(

        @NotNull(message = "Effective from date is required")
        LocalDate effectiveFrom,

        @NotNull(message = "Effective to date is required")
        LocalDate effectiveTo,

        @NotNull(message = "Fee is required")
        @DecimalMin(value = "0.00", inclusive = false, message = "Fee must be greater than zero")
        BigDecimal fee,

        @NotNull(message = "Created by user is required")
        Long createdBy
) {
    @JsonIgnore
    @AssertTrue(message = "Effective from date must be on or before effective to date")
    public boolean isEffectiveDateRangeValid() {
        return effectiveFrom != null && effectiveTo != null && !effectiveFrom.isAfter(effectiveTo());
    }
    @JsonIgnore 
    @AssertTrue(message = "Effective from date must be the first day of a month and effective to date must be the last day of a month")
    public boolean isMonthlyBoundaryValid() {
        return effectiveFrom != null
                && effectiveTo != null
                && effectiveFrom.getDayOfMonth() == 1
                && effectiveTo.getDayOfMonth() == effectiveTo.lengthOfMonth();
    }

}
