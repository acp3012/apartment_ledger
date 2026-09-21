package com.yesh.apartmentledger.finance.income.controller;

import com.yesh.apartmentledger.finance.income.dto.IncomeApprovalRequest;
import com.yesh.apartmentledger.finance.income.dto.IncomeDraftRequest;
import com.yesh.apartmentledger.finance.income.dto.IncomeDraftResponse;
import com.yesh.apartmentledger.finance.income.service.IncomeDraftService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/finance")
public class IncomeController {

    private final IncomeDraftService incomeDraftService;

    public IncomeController(IncomeDraftService incomeDraftService) {
        this.incomeDraftService = incomeDraftService;
    }

    /**
     * UI Action: Admin submits the Income Entry form
     * Endpoint: POST /al/api/v1/finance/income-drafts
     */
    @PostMapping("/income-drafts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<IncomeDraftResponse> createIncomeDraft(@Valid @RequestBody IncomeDraftRequest request) {
        IncomeDraftResponse response = incomeDraftService.createIncomeDraft(request);
        return ResponseEntity.ok(response);
    }

    /**
     * UI Action: Load the Approval Dashboard table with pending entries
     * Endpoint: GET /al/api/v1/finance/income-drafts/pending
     */
    @GetMapping("/income-drafts/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<IncomeDraftResponse>> getPendingDrafts() {
        List<IncomeDraftResponse> pendingDrafts = incomeDraftService.getPendingDrafts();
        return ResponseEntity.ok(pendingDrafts);
    }

    /**
     * UI Action: Admin clicks "Approve" on a pending draft
     * Endpoint: PUT /al/api/v1/finance/income-drafts/{id}/approve
     */
    @PutMapping("/income-drafts/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> approveDraft(
            @PathVariable Long id,
            @RequestBody(required = false) IncomeApprovalRequest request) {
        incomeDraftService.approveDraft(id, request);
        return ResponseEntity.ok().build();
    }

    /**
     * UI Action: Admin clicks "Reject" on a pending draft
     * Endpoint: PUT /al/api/v1/finance/income-drafts/{id}/reject
     */
    @PutMapping("/income-drafts/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> rejectDraft(
            @PathVariable Long id,
            @RequestBody(required = false) IncomeApprovalRequest request) {
        incomeDraftService.rejectDraft(id, request);
        return ResponseEntity.ok().build();
    }
}