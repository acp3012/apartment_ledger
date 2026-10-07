package com.yesh.apartmentledger.core.flat.controller;

import com.yesh.apartmentledger.core.flat.dto.FlatPaymentStatusResponse;
import com.yesh.apartmentledger.core.flat.dto.FlatResponse;
import com.yesh.apartmentledger.core.flat.repository.FlatRepository;
import com.yesh.apartmentledger.finance.income.service.IncomeService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/apartments/{apartmentId}/flats")
public class FlatController {

    private final FlatRepository flatRepository;
    private final IncomeService incomeService ;

    @GetMapping()
    public ResponseEntity<List<FlatResponse>> getFlatsByApartment(@PathVariable Long apartmentId) {

        List<FlatResponse> flats = flatRepository.findByApartmentId(apartmentId)
                .stream()
                .map(flat -> new FlatResponse(flat.getId(), flat.getFlatNumber(), flat.getOwnerName())) // Map entity to DTO
                .collect(Collectors.toList());

        return ResponseEntity.ok(flats);
    }
    @GetMapping("/{flatId}/payment-status")
    public ResponseEntity<FlatPaymentStatusResponse> getFlatPaymentStatus(
            @PathVariable Long apartmentId,
            @PathVariable Long flatId,
            @RequestParam(required = false) Short year,
            @RequestParam(required = false) Short month
    ) {

       return ResponseEntity.ok(incomeService.getFlatMaintenancePaymentStatus(apartmentId,flatId,year,month));

    }
}