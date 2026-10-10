package com.yesh.apartmentledger.finance.ledger.controller;

import com.yesh.apartmentledger.finance.ledger.dto.LedgerPeriodResponse;
import com.yesh.apartmentledger.finance.ledger.dto.LedgerSummaryResponse;
import com.yesh.apartmentledger.finance.ledger.dto.MonthEndCloseResponse;
import com.yesh.apartmentledger.finance.ledger.dto.MonthlyLedgerRequest;
import com.yesh.apartmentledger.finance.ledger.entity.LedgerDetail;
import com.yesh.apartmentledger.finance.ledger.entity.MonthlyLedger;
import com.yesh.apartmentledger.finance.ledger.repository.LedgerDetailRepository;
import com.yesh.apartmentledger.finance.ledger.service.LedgerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@Tag(name = "Ledger management", description = "Manage apartment ledger periods, balances, details, and summaries.")
public class LedgerController {

    private final LedgerService ledgerService;
    private final LedgerDetailRepository ledgerDetailRepository;

        @Operation(summary = "Get the active ledger period",
            description = "Returns the currently active ledger period for the specified apartment.")
    @GetMapping("/active-period")
        public ResponseEntity<LedgerPeriodResponse> getActiveLedgerPeriod(
            @Parameter(description = "ID of the apartment.") @PathVariable Long apartmentId) {

        return ResponseEntity.ok(ledgerService.getActiveLedgerPeriod(apartmentId));
    }

    //==============================================
    // Get income and expense summary for a period
    //================================================
        @Operation(summary = "Get ledger details",
            description = "Returns the ledger entries for an apartment in the specified year and month.")
    @GetMapping("/details")
    public ResponseEntity<List<LedgerDetail>>  getLedgerDetails(
            @Parameter(description = "ID of the apartment.") @PathVariable Long apartmentId,
            @Parameter(description = "Calendar year of the ledger period.") @RequestParam Short year,
            @Parameter(description = "Month of the ledger period, from 1 to 12.") @RequestParam Short month) {

        return ResponseEntity.ok(ledgerDetailRepository
                .findByApartmentAndOptionalPeriod(apartmentId, year,month));
    }
    // Ledger Summary
        @Operation(summary = "Get ledger summary",
            description = "Returns income, expense, and balance totals for an apartment's specified ledger month.")
    @GetMapping("/summary")
    public ResponseEntity<LedgerSummaryResponse>  getLedgerSummary(
            @Parameter(description = "ID of the apartment.") @PathVariable Long apartmentId,
            @Parameter(description = "Calendar year of the ledger period.") @RequestParam Short year,
            @Parameter(description = "Month of the ledger period, from 1 to 12.") @RequestParam Short month) {

        return ResponseEntity.ok(ledgerService.getLedgerSummary(apartmentId,year,month));

    }

    // Summary
    @Operation(summary = "Preview month-end ledger close",
        description = "Calculates the month-end close result for the specified period without closing it.")
    @GetMapping("/preview")
    public ResponseEntity<MonthEndCloseResponse> previewClose(
        @Parameter(description = "ID of the apartment.") @PathVariable Long apartmentId,
        @Parameter(description = "Calendar year of the ledger period.") @RequestParam Short year,
        @Parameter(description = "Month of the ledger period, from 1 to 12.") @RequestParam Short month) {
        return ResponseEntity.ok(ledgerService.previewMonthEndClose(apartmentId, year, month));
    }
    //====================================
    // POST calls
    //======================================
        @Operation(summary = "Create a monthly ledger",
            description = "Creates the apartment's monthly ledger using the supplied opening-balance details.")
    @PostMapping("/opening-balance")
    public ResponseEntity<MonthlyLedger> createMonthlyLedger(
            @Parameter(description = "ID of the apartment.") @PathVariable Long apartmentId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Opening-balance details for the monthly ledger.", required = true)
            @RequestBody MonthlyLedgerRequest monthlyLedgerRequest){
        var response =  ledgerService.createMonthlyLedger(apartmentId,monthlyLedgerRequest);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()    // Captures "http://localhost:8080/api/users"
                .path("/{id}")          // Appends "/{id}" placeholder
                .buildAndExpand(response.getId()) // Replaces "{id}" with the actual entity ID
                .toUri();
        return ResponseEntity.created(location).body(response);

    }

        @Operation(summary = "Close a monthly ledger period",
            description = "Closes the specified apartment ledger period and returns a confirmation message.")
    @PostMapping("/close")
    public ResponseEntity<String> closeMonthlyLeger(
            @Parameter(description = "ID of the apartment whose ledger period is being closed.")
            @RequestParam Long apartmentId,
            @Parameter(description = "Calendar year of the ledger period.") @RequestParam Short year,
            @Parameter(description = "Month of the ledger period, from 1 to 12.") @RequestParam Short month) {
        return ResponseEntity.ok(ledgerService.closeActiveAccountPeriod(apartmentId, year, month));
    }
}
