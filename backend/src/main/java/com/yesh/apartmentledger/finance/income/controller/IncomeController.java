package com.yesh.apartmentledger.finance.income.controller;

import com.yesh.apartmentledger.core.validation.ValidMonth;
import com.yesh.apartmentledger.core.enums.ApprovalStatusEnum;
import com.yesh.apartmentledger.finance.income.dto.*;
import com.yesh.apartmentledger.finance.income.service.IncomeDraftService;
//import com.yesh.apartmentledger.finance.income.service.IncomeService;
import com.yesh.apartmentledger.finance.income.service.IncomeService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/apartments/{apartmentId}/incomes")
@PreAuthorize("@apartmentAuthorization.canAccess(#p0)")
@Tag(name= "Income management.",
        description = "API to manage income records of the apartment. Draft are unapproved entries. Once approved it becomes income.")

public class IncomeController {

    private final IncomeDraftService incomeDraftService;
    private final IncomeService incomeService;

    // 1. CREATE: Used by the React Maker Form you just built
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/drafts")
    public ResponseEntity<String> createDraft(
            @PathVariable Long apartmentId,
            @Valid @RequestBody IncomeDraftRequest request) {
        incomeDraftService.createDraft(apartmentId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body("Income draft saved successfully.");
    }
    // Update draft income
     @PreAuthorize("hasRole('ADMIN')")

    @PutMapping("/drafts/{draftId}")
    public ResponseEntity<Void> updateIncomeDraft(
            @PathVariable Long apartmentId,
            @PathVariable Long draftId,
            @Valid @RequestBody IncomeDraftRequest payload) {

        // Your service should find the draft by draftId, update its fields with the payload, and save()
        incomeDraftService.updateDraft(apartmentId, draftId, payload);

        return ResponseEntity.ok().build();
    }
   
    @GetMapping("/drafts")
    public ResponseEntity<List<IncomeDraftResponse>> getAllDrafts(
            @PathVariable Long apartmentId,
            @RequestParam(required = false) String approvalStatus,
            @RequestParam(required = false) Short year,
            @ValidMonth @RequestParam(required = false) Short month
    ) {
        List<IncomeDraftResponse> response = incomeDraftService
                .getDraftsByApprovalStatusYearAndMonth(apartmentId,approvalStatus,year,month);
        return ResponseEntity.ok(response);
    }

    // 3. UPDATE: Approve a batch of drafts
    @PreAuthorize("hasRole('ADMIN') and @apartmentAuthorization.canAccess(#p0)")
    @PostMapping("/drafts/approve-bulk")
    public ResponseEntity<String> approveDrafts(
            @PathVariable Long apartmentId,
            @RequestBody(required = true) DraftApprovalRequest request) {

        String result = incomeDraftService.draftApprovalDecision(apartmentId, request,ApprovalStatusEnum.APPROVED);
        return ResponseEntity.ok(result);
    }
    
    @PreAuthorize("hasRole('ADMIN') and @apartmentAuthorization.canAccess(#p0)")
    @PostMapping("/drafts/reject-bulk")
    public ResponseEntity<String> rejectDrafts(
            @PathVariable Long apartmentId,
            @RequestBody(required = true) DraftApprovalRequest request) {

        String result = incomeDraftService.draftApprovalDecision(apartmentId, request, ApprovalStatusEnum.REJECTED);
        return ResponseEntity.ok(result);
    }

    // ========================= income -------------------
    // al/api/v1/apartments/1/incomes?year=2026&month=8
    @GetMapping
    public ResponseEntity<List<IncomeResponse>> getAllIncome(
            @PathVariable Long apartmentId,
            @RequestParam(required = false) Short year,
            @ValidMonth @RequestParam(required = false) Short month
    ) {
        var data =  incomeService.getIncomeByApartmentYearAndMonth(apartmentId,year,month);
        return ResponseEntity.ok(data);
    }
    // for reports
    @GetMapping("/summary/payment-mode")
    public ResponseEntity<List<PaymentModeIncomeSummary>> getSummaryByPaymentMode(
            @PathVariable Long apartmentId,
            @RequestParam Short year,
            @ValidMonth @RequestParam Short month) {

        return ResponseEntity.ok(incomeService.getIncomeSummaryByPaymentMode(apartmentId,year,month));
    }


}