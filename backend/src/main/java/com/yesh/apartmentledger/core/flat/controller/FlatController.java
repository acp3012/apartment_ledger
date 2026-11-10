package com.yesh.apartmentledger.core.flat.controller;

import com.yesh.apartmentledger.core.validation.ValidMonth;
import com.yesh.apartmentledger.core.flat.dto.FlatPaymentStatusResponse;
import com.yesh.apartmentledger.core.flat.dto.FlatRequest;
import com.yesh.apartmentledger.core.flat.dto.FlatResponse;
import com.yesh.apartmentledger.core.flat.service.FlatService;
import com.yesh.apartmentledger.finance.income.service.IncomeService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/apartments/{apartmentId}/flats")
@PreAuthorize("@apartmentAuthorization.canAccess(#p0)")
public class FlatController {

    //private final FlatRepository flatRepository;
    private final FlatService flatService;
    private final IncomeService incomeService ;
    
    @PostMapping()
    public ResponseEntity<FlatResponse> createFlat(
            @PathVariable Long apartmentId,
           @Valid @RequestBody FlatRequest request) {
        
        var response = flatService.createFlat(apartmentId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{flatId}")
    public ResponseEntity<FlatResponse> updateFlat(
            @PathVariable Long apartmentId,
            @PathVariable Long flatId,
            @Valid @RequestBody FlatRequest request) {

        return ResponseEntity.ok(flatService.updateFlat(apartmentId, flatId, request));
    }

    @GetMapping()
    public ResponseEntity<List<FlatResponse>> getFlatsByApartment(@PathVariable Long apartmentId) {
        
        return ResponseEntity.ok(flatService.getFlatsByApartment(apartmentId));
    }

    @GetMapping("/{flatId}/payment-status")
    public ResponseEntity<FlatPaymentStatusResponse> getFlatPaymentStatus(
            @PathVariable Long apartmentId,
            @PathVariable Long flatId,
            @RequestParam Short year,
            @ValidMonth @RequestParam Short month
    ) {

       return ResponseEntity.ok(incomeService.getFlatMaintenancePaymentStatus(apartmentId,flatId,year,month));

    }
}