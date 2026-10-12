package com.yesh.apartmentledger.finance.maintenance.controller;

import com.yesh.apartmentledger.finance.maintenance.dto.MaintenanceRateRequest;
import com.yesh.apartmentledger.finance.maintenance.dto.MaintenanceRateResponse;
import com.yesh.apartmentledger.finance.maintenance.service.MaintenanceRateService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;


@RestController
@RequestMapping("/api/v1/apartments/{apartmentId}/maintenance-rates")
@AllArgsConstructor
@PreAuthorize("@apartmentAuthorization.canAccess(#p0)")
@Tag(name = "Maintenance fee management.", description = "Endpoints for managing maintenance fees for apartments and flats")
public class  MaintenanceRateController {

    private final MaintenanceRateService rateService;



    @GetMapping("/current")
    public ResponseEntity<MaintenanceRateResponse> getApplicableRate(
            @PathVariable Long apartmentId,
            @RequestParam Long flatId,
            @RequestParam LocalDate date) {
       return ResponseEntity.ok(rateService.getMaintenanceFee(apartmentId,flatId,date));
    }

    @PostMapping()
    @PreAuthorize("hasRole('ADMIN')")
    @Operation (summary = "Create a new maintenance fee for an apartment. If flatId is provided, it will be a flat-specific rate; otherwise, it will be an apartment-wide rate.")
    public ResponseEntity<MaintenanceRateResponse> createMaintenanceFee(
            @PathVariable Long apartmentId,
            @RequestBody MaintenanceRateRequest request) {
        return ResponseEntity.created(null).body(rateService.createMaintenanceFee(apartmentId, null, request));
    }

    @PostMapping("/flats/{flatId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation (summary = "Create a new maintenance fee for a specific flat within an apartment. If flatId is provided, it will be a flat-specific rate; otherwise, it will be an apartment-wide rate.")
    public ResponseEntity<MaintenanceRateResponse> createMaintenanceFee(
            @PathVariable Long apartmentId,
            @PathVariable Long flatId,
            @RequestBody MaintenanceRateRequest request) {
        return ResponseEntity.created(null).body(rateService.createMaintenanceFee(apartmentId, flatId, request));
    }
    
}