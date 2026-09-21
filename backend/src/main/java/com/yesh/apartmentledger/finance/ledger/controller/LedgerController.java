package com.yesh.apartmentledger.finance.ledger.controller;

import com.yesh.apartmentledger.finance.ledger.dto.LedgerPeriodResponse;
import com.yesh.apartmentledger.finance.ledger.dto.MonthEndCloseResponse;
import com.yesh.apartmentledger.finance.ledger.service.LedgerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/apartments/{apartmentId}/ledger")
@RequiredArgsConstructor
public class LedgerController {

    private final LedgerService ledgerService;
    @GetMapping("/active-period")
    public ResponseEntity<LedgerPeriodResponse> getActiveLedgerPeriod(@PathVariable Long apartmentId){

        return ResponseEntity.ok(ledgerService.getLedgerPeriod(apartmentId));
    }

    @GetMapping("/preview")
    public ResponseEntity<MonthEndCloseResponse> previewClose(
            @PathVariable  Long apartmentId,
            @RequestParam Short year,
            @RequestParam Short month) {
        return ResponseEntity.ok(ledgerService.previewMonthEndClose(apartmentId, year, month));
    }

    @PostMapping("/ledger/close")
    public ResponseEntity<String> submitClose(
            @RequestParam Long apartmentId,
            @RequestParam Short year,
            @RequestParam Short month) {
        return ResponseEntity.ok(ledgerService.closeMonth(apartmentId, year, month));
    }
}
