package com.yesh.apartmentledger.finance.ledger.controller;

import com.yesh.apartmentledger.finance.ledger.dto.LedgerPeriodResponse;
import com.yesh.apartmentledger.finance.ledger.dto.LedgerSummaryResponse;
import com.yesh.apartmentledger.finance.ledger.dto.MonthEndCloseResponse;
import com.yesh.apartmentledger.finance.ledger.entity.LedgerDetail;
import com.yesh.apartmentledger.finance.ledger.repository.LedgerDetailRepository;
import com.yesh.apartmentledger.finance.ledger.service.LedgerService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/apartments/{apartmentId}/ledger")
@RequiredArgsConstructor
@Tag(name = "Ledger report", description = "Provides month wise income and expenses along with balance. ")
public class LedgerController {

    private final LedgerService ledgerService;
    private final LedgerDetailRepository ledgerDetailRepository;


    @GetMapping("/active-period")
    public ResponseEntity<LedgerPeriodResponse> getActiveLedgerPeriod(@PathVariable Long apartmentId){

        return ResponseEntity.ok(ledgerService.getLedgerPeriod(apartmentId));
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

        return ResponseEntity.ok(ledgerService.getSummary(apartmentId,year,month));

    }

    // Summary
    @GetMapping("/preview")
    public ResponseEntity<MonthEndCloseResponse> previewClose(
            @PathVariable  Long apartmentId,
            @RequestParam Short year,
            @RequestParam Short month) {
        return ResponseEntity.ok(ledgerService.previewMonthEndClose(apartmentId, year, month));
    }

    @PostMapping("/close")
    public ResponseEntity<String> submitClose(
            @RequestParam Long apartmentId,
            @RequestParam Short year,
            @RequestParam Short month) {
        return ResponseEntity.ok(ledgerService.closeMonth(apartmentId, year, month));
    }


}
