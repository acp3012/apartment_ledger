package com.yesh.apartmentledger.finance.maintenance.dto;

import java.math.BigDecimal;


public record MaintenanceRateResponse (
        Long apartmentId,
        Long flatId,
    BigDecimal fee
){}
