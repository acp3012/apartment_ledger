package com.yesh.apartmentledger.finance.ledger.controller;

import com.yesh.apartmentledger.finance.ledger.dto.LedgerPeriodResponse;
import com.yesh.apartmentledger.finance.ledger.dto.LedgerSummaryResponse;
import com.yesh.apartmentledger.finance.ledger.dto.MonthEndCloseResponse;
import com.yesh.apartmentledger.finance.ledger.dto.MonthlyLedgerRequest;
import com.yesh.apartmentledger.finance.ledger.entity.LedgerDetail;
import com.yesh.apartmentledger.finance.ledger.entity.MonthlyLedger;
import com.yesh.apartmentledger.finance.ledger.repository.LedgerDetailRepository;
import com.yesh.apartmentledger.finance.ledger.service.LedgerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/apartments/{apartmentId}/ledger")
@RequiredArgsConstructor
@Tag(name = "Ledger management", description = "Crate onetime ledger setup and provides ledger related services. ")
public class LedgerController {

    private final LedgerService ledgerService;
    private final LedgerDetailRepository ledgerDetailRepository;

    @GetMapping("/active-period")
    public ResponseEntity<LedgerPeriodResponse> getActiveLedgerPeriod(@PathVariable Long apartmentId){

        return ResponseEntity.ok(ledgerService.getActiveLedgerPeriod(apartmentId));
    }

    //==============================================
    // Get income and expense summary for a period
    //================================================
    @GetMapping("/details")
    public ResponseEntity<List<LedgerDetail>>  getLedgerDetails(
            @PathVariable Long apartmentId,
            @RequestParam Short year,
            @RequestParam Short month) {

        return ResponseEntity.ok(ledgerDetailRepository
                .findByApartmentAndOptionalPeriod(apartmentId, year,month));
    }
    // Ledger Summary
    @GetMapping("/summary")
    public ResponseEntity<LedgerSummaryResponse>  getLedgerSummary(
            @PathVariable Long apartmentId,
            @RequestParam Short year,
            @RequestParam Short month) {

        return ResponseEntity.ok(ledgerService.getLedgerSummary(apartmentId,year,month));

    }

    // Summary
    @GetMapping("/preview")
    public ResponseEntity<MonthEndCloseResponse> previewClose(
            @PathVariable  Long apartmentId,
            @RequestParam Short year,
            @RequestParam Short month) {
        return ResponseEntity.ok(ledgerService.previewMonthEndClose(apartmentId, year, month));
    }
    //====================================
    // POST calls
    //======================================
    @PostMapping("/opening-balance")
    public ResponseEntity<MonthlyLedger> createMonthlyLedger(
            @PathVariable Long apartmentId,
            @RequestBody MonthlyLedgerRequest monthlyLedgerRequest){
        var response =  ledgerService.createMonthlyLedger(apartmentId,monthlyLedgerRequest);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()    // Captures "http://localhost:8080/api/users"
                .path("/{id}")          // Appends "/{id}" placeholder
                .buildAndExpand(response.getId()) // Replaces "{id}" with the actual entity ID
                .toUri();
        return ResponseEntity.created(location).body(response);

    }

    @PostMapping("/close")
    public ResponseEntity<String> closeMonthlyLeger(
            @RequestParam Long apartmentId,
            @RequestParam Short year,
            @RequestParam Short month) {
        return ResponseEntity.ok(ledgerService.closeActiveAccountPeriod(apartmentId, year, month));
    }
}
