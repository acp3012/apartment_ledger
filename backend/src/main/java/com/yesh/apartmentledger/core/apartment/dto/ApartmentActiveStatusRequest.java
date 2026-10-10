package com.yesh.apartmentledger.core.apartment.dto;

import jakarta.validation.constraints.NotNull;

public record ApartmentActiveStatusRequest(@NotNull Boolean isActive) {
}