package com.yesh.apartmentledger.finance.income.controller;

import com.yesh.apartmentledger.finance.income.dto.DraftApprovalRequest;
import com.yesh.apartmentledger.finance.income.dto.IncomeDraftRequest; // The DTO we defined earlier
import com.yesh.apartmentledger.finance.income.dto.IncomeDraftResponse;
import com.yesh.apartmentledger.finance.income.entity.IncomeDraft;
import com.yesh.apartmentledger.finance.income.service.IncomeDraftService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
// Standardized base path grouping by tenant, then module, then resource
@RequestMapping("/api/v1/apartments/{apartmentId}/income")
public class IncomeDraftController {

    private final IncomeDraftService incomeDraftService;

    public IncomeDraftController(IncomeDraftService incomeDraftService) {
        this.incomeDraftService = incomeDraftService;

    }

    // 1. CREATE: Used by the React Maker Form you just built
    @PostMapping("/draft")
    public ResponseEntity<String> createDraft(
            @PathVariable Long apartmentId,
            @RequestBody IncomeDraftRequest request) {

        incomeDraftService.createDraft(apartmentId, request);
        return ResponseEntity.ok("Income draft saved successfully.");
    }
    // Update draft income
    @PutMapping("/draft/{draftId}")
    public ResponseEntity<Void> updateIncomeDraft(
            @PathVariable Long apartmentId,
            @PathVariable Long draftId,
            @RequestBody IncomeDraftRequest payload) {

        // Your service should find the draft by draftId, update its fields with the payload, and save()
        incomeDraftService.updateDraft(apartmentId, draftId, payload);

        return ResponseEntity.ok().build();
    }
    // 2. READ: Get pending drafts for the grid/Checker
    @GetMapping("/draft/pending")
    public ResponseEntity<List<IncomeDraftResponse>> getPendingDrafts(
            @PathVariable Long apartmentId) {
        List<IncomeDraftResponse> response = incomeDraftService.getPendingDrafts(apartmentId);
        return ResponseEntity.ok(response);
    }

    // 3. UPDATE: Approve a batch of drafts
    @PostMapping("/drawft/{id}/approve")
    public ResponseEntity<String> approveDrafts(
            @PathVariable Long apartmentId,
            @PathVariable Long id,
            @RequestBody(required = true) DraftApprovalRequest request) {

        String result = incomeDraftService.approveDrafts(id, request);
        return ResponseEntity.ok(result);
    }
}