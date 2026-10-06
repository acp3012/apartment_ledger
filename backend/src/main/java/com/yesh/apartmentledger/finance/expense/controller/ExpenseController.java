package com.yesh.apartmentledger.finance.expense.controller;

import com.yesh.apartmentledger.finance.expense.dto.*;
import com.yesh.apartmentledger.finance.expense.entity.Expense;
import com.yesh.apartmentledger.finance.expense.service.ExpenseService;
import com.yesh.apartmentledger.finance.income.dto.PaymentModeIncomeSummary;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ExpenseController
 */
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/apartments/{apartmentId}/expenses")

@Tag(name= "Expense Management.",
        description = "API to manage expense record of the apartment. Draft are unapproved entries. Once approved it becomes expense")

public class ExpenseController {

    private final ExpenseService expenseService;
    // =====================================================
    // DRAFTs
    //====================================================
    // Create drafts
    //==================================================
    @PostMapping("/drafts")
    public ResponseEntity<String> createDraft(
            @PathVariable Long apartmentId,
            @RequestBody ExpenseDraftRequest request) {
        expenseService.createExpenseDraft(apartmentId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body("Expense draft saved successfully.");
    }
    // Update draft by id
    @PutMapping("/drafts/{draftId}")
    public ResponseEntity<String> updateExpenseDraft(
            @PathVariable Long apartmentId,
            @PathVariable Long draftId,
            @RequestBody ExpenseDraftRequest payload) {

        expenseService.updateDraft(apartmentId, draftId, payload);

        return ResponseEntity.status(HttpStatus.OK).body("Draft updated successfully.");

    }
    // GET drafts
    // /api/v1/apartments/{apartmentId}/expenses/drafts?approvalStatus=PENDING&year=2026&month=8
    @GetMapping("/drafts")
    public ResponseEntity<List<ExpenseDraftResponse>> getAllDrafts(
            @PathVariable Long apartmentId,
            @RequestParam(required = false) String approvalStatus,
            @RequestParam(required = false) Short year,
            @RequestParam(required = false) Short month) {
        return ResponseEntity.ok(expenseService.getDraftsByApprovalStatusYearAndMonth(apartmentId, approvalStatus, year, month));
    }
    // GET fetch draft by id
    // /api/v1/apartments/{apartmentId}/expenses/drafts/{draftId}``
    @GetMapping("/drafts/{draftId}")
    public ResponseEntity<ExpenseDraftResponse> getDraftById(
            @PathVariable Long apartmentId,
            @PathVariable Long draftId) {
        return ResponseEntity.ok(expenseService.getDraftById(apartmentId, draftId));
    }


    @PostMapping("/drafts/approve-bulk")
    public ResponseEntity<String> approveDrafts(
            @PathVariable Long apartmentId,
            @RequestBody ExpenseApprovalRequest request) {
        return ResponseEntity.ok(expenseService.approveDrafts(apartmentId, request));
    }

    @PostMapping("/drafts/reject-bulk")
    public ResponseEntity<String> rejectDrafts(
            @PathVariable Long apartmentId,
            @RequestBody ExpenseApprovalRequest request) {
        return ResponseEntity.ok(expenseService.rejectDrafts(apartmentId, request));
    }
    //====================================================
    // GET Approved Expenses (Read-only)
    //====================================================
    // /api/v1/apartments/{apartmentId}/expenses?year=2026&month=8
    @GetMapping
    public ResponseEntity<List<ExpenseResponse>> getAllExpenses(
            @PathVariable Long apartmentId,
            @RequestParam(required = false) Short year,
            @RequestParam(required = false) Short month) {
        return ResponseEntity.ok(expenseService.getExpenseByApartmentYearAndMonth(apartmentId, year, month));
    }

    // for reports
    @GetMapping("/summary/ledger-category")
    public ResponseEntity<List<LedgerCategoryExpenseSummary>> getSummaryByPaymentMode(
            @PathVariable Long apartmentId,
            @RequestParam Short year,
            @RequestParam Short month) {

        return ResponseEntity.ok(expenseService.getExpenseSummaryByCategory(apartmentId,year,month));
    }

}