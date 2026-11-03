package com.yesh.apartmentledger.core.apartment.dto;

import com.yesh.apartmentledger.core.validation.ValidMonth;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record GoLivePeriodRequest(
        @NotNull @Min(2000) @Max(2100) Short year,
        @NotNull @ValidMonth Short month
) {
}
