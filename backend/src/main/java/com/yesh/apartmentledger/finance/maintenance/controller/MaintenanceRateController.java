package com.yesh.apartmentledger.finance.maintenance.controller;

import com.yesh.apartmentledger.finance.maintenance.dto.MaintenanceRateResponse;
import com.yesh.apartmentledger.finance.maintenance.entity.MaintenanceRate;
import com.yesh.apartmentledger.finance.maintenance.service.MaintenanceRateService;
import com.yesh.apartmentledger.finance.maintenance.service.MaintenanceRateService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/apartments/{apartmentId}/maintenance-rates")
@AllArgsConstructor
public class  MaintenanceRateController {

    private final MaintenanceRateService rateService;



    @GetMapping("/current")
    public ResponseEntity<MaintenanceRateResponse> getApplicableRate(
            @PathVariable Long apartmentId,
            @RequestParam Long flatId,
            @RequestParam LocalDate date) {
        // Priority 1: Flat-specific rate. Priority 2: Apartment-wide rate (flatId is null)
        // System.out.println("DEBUG: Hit Maintenance Rate Endpoint for Apt: " + apartmentId + ", Flat: " + flatId + ", Date: " + date);
           return ResponseEntity.ok(rateService.getMaintenanceFee(apartmentId,flatId,date));
    }
}