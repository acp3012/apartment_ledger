package com.yesh.apartmentledger.master.controller;

import com.yesh.apartmentledger.master.approvalstatus.entity.ApprovalStatus;
import com.yesh.apartmentledger.master.cache.LookupCacheService;
import com.yesh.apartmentledger.master.ledgercategory.entity.LedgerCategory;
import com.yesh.apartmentledger.master.paymentmode.entity.PaymentMode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// this endpoints not for users or frontend.
@RestController
@RequestMapping("/api/v1/apartments/lookups")
public class LookupCacheController {

    private final LookupCacheService cacheService;

    public LookupCacheController(LookupCacheService cacheService) {
        this.cacheService = cacheService;
    }

    @GetMapping("/approval-statuses")
    public ResponseEntity<List<ApprovalStatus>> getApprovalStatuses() {
        return ResponseEntity.ok(cacheService.getApprovalStatuses());
    }
    @GetMapping("/approval-statuses/{id}")
    public ResponseEntity<ApprovalStatus> getApprovalStatusById(@PathVariable Long id) {
        return ResponseEntity.ok(cacheService.getApprovalStatusById(id));
    }

    @GetMapping("/ledger-categories")
    public ResponseEntity<List<LedgerCategory>> getCategories() {
        return ResponseEntity.ok(cacheService.getLedgerCategories());
    }

    @GetMapping("/ledger-categories/{id}")
    public ResponseEntity<LedgerCategory> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(cacheService.getLedgerCategoryById(id));
    }

    @GetMapping("/payment-modes")
    public ResponseEntity<List<PaymentMode>> getPaymentModes() {
              return ResponseEntity.ok(cacheService.getPaymentModes());
    }

    @GetMapping("/payment-modes/{id}")
    public ResponseEntity<PaymentMode> getPaymentModeById(@PathVariable Long id) {
        return ResponseEntity.ok(cacheService.getPaymentModeById(id));
    }

    //========================================
    // POST Refresh Cache
    //#######################################

    @PostMapping("/refresh-cache")
    public ResponseEntity<String> refreshCache() {
        cacheService.refreshCache();
        return ResponseEntity.ok("Lookup cache refreshed successfully from database.");
    }
}