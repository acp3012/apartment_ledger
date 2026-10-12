package com.yesh.apartmentledger.finance.maintenance.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;


public record MaintenanceRateResponse (
        Long maintenanceRateId,
        Long apartmentId,
        Long flatId,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        BigDecimal fee,
        LocalDateTime createdDate,
        LocalDateTime lastModifiedDate
) {
   
}
