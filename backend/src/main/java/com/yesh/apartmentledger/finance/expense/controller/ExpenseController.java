package com.yesh.apartmentledger.finance.expense.controller;

import com.yesh.apartmentledger.finance.expense.dto.*;
import com.yesh.apartmentledger.finance.expense.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/apartments/{apartmentId}/expenses")

@Tag(name = "Expense management",
    description = "Manage expense drafts, approvals, approved expenses, and expense summaries for an apartment.")

public class ExpenseController {

    private final ExpenseService expenseService;
    // =====================================================
    // DRAFTs
    //====================================================
    // Create drafts
    //==================================================
    @Operation(summary = "Create an expense draft",
            description = "Creates an unapproved expense draft for the specified apartment.")
    @PostMapping("/drafts")
    public ResponseEntity<String> createDraft(
            @Parameter(description = "ID of the apartment.") @PathVariable Long apartmentId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Expense details to save as a draft.", required = true)
            @RequestBody ExpenseDraftRequest request) {
        expenseService.createExpenseDraft(apartmentId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body("Expense draft saved successfully.");
    }
    // Update draft by id
        @Operation(summary = "Update an expense draft",
            description = "Updates the details of the specified expense draft for the apartment.")
    @PutMapping("/drafts/{draftId}")
    public ResponseEntity<String> updateExpenseDraft(
            @Parameter(description = "ID of the apartment.") @PathVariable Long apartmentId,
            @Parameter(description = "ID of the expense draft to update.") @PathVariable Long draftId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Updated expense details for the draft.", required = true)
            @RequestBody ExpenseDraftRequest payload) {

        expenseService.updateDraft(apartmentId, draftId, payload);

        return ResponseEntity.status(HttpStatus.OK).body("Draft updated successfully.");

    }
    // GET drafts
    // /api/v1/apartments/{apartmentId}/expenses/drafts?approvalStatus=PENDING&year=2026&month=8
        @Operation(summary = "List expense drafts",
            description = "Returns expense drafts for an apartment, optionally filtered by approval status, year, and month.")
    @GetMapping("/drafts")
    public ResponseEntity<List<ExpenseDraftResponse>> getAllDrafts(
            @Parameter(description = "ID of the apartment.") @PathVariable Long apartmentId,
            @Parameter(description = "Optional approval status to filter drafts.")
            @RequestParam(required = false) String approvalStatus,
            @Parameter(description = "Optional calendar year to filter drafts.")
            @RequestParam(required = false) Short year,
            @Parameter(description = "Optional month, from 1 to 12, to filter drafts.")
            @Min(1) @Max(12)
            @RequestParam(required = false) Short month) {
        return ResponseEntity.ok(expenseService.getDraftsByApprovalStatusYearAndMonth(apartmentId, approvalStatus, year, month));
    }
    // GET fetch draft by id
    // /api/v1/apartments/{apartmentId}/expenses/drafts/{draftId}``
        @Operation(summary = "Get an expense draft",
            description = "Returns the specified expense draft belonging to the apartment.")
    @GetMapping("/drafts/{draftId}")
    public ResponseEntity<ExpenseDraftResponse> getDraftById(
            @Parameter(description = "ID of the apartment.") @PathVariable Long apartmentId,
            @Parameter(description = "ID of the expense draft to retrieve.") @PathVariable Long draftId) {
        return ResponseEntity.ok(expenseService.getDraftById(apartmentId, draftId));
    }


    @Operation(summary = "Approve expense drafts",
        description = "Approves the drafts identified in the request for the specified apartment.")
    @PostMapping("/drafts/approve-bulk")
    public ResponseEntity<String> approveDrafts(
        @Parameter(description = "ID of the apartment.") @PathVariable Long apartmentId,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Expense draft identifiers to approve.", required = true)
            @RequestBody ExpenseApprovalRequest request) {
        return ResponseEntity.ok(expenseService.approveDrafts(apartmentId, request));
    }

        @Operation(summary = "Reject expense drafts",
            description = "Rejects the drafts identified in the request for the specified apartment.")
    @PostMapping("/drafts/reject-bulk")
    public ResponseEntity<String> rejectDrafts(
            @Parameter(description = "ID of the apartment.") @PathVariable Long apartmentId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Expense draft identifiers to reject.", required = true)
            @RequestBody ExpenseApprovalRequest request) {
        return ResponseEntity.ok(expenseService.rejectDrafts(apartmentId, request));
    }
    //====================================================
    // GET Approved Expenses (Read-only)
    //====================================================
    // /api/v1/apartments/{apartmentId}/expenses?year=2026&month=8
        @Operation(summary = "List approved expenses",
            description = "Returns approved expenses for an apartment, optionally filtered by year and month.")
    @GetMapping
    public ResponseEntity<List<ExpenseResponse>> getAllExpenses(
            @Parameter(description = "ID of the apartment.") @PathVariable Long apartmentId,
            @Parameter(description = "Optional calendar year to filter expenses.")
            @RequestParam(required = false) Short year,
            @Parameter(description = "Optional month, from 1 to 12, to filter expenses.")
            @Min(1) @Max(12)
            @RequestParam(required = false) Short month) {
        return ResponseEntity.ok(expenseService.getExpenseByApartmentYearAndMonth(apartmentId, year, month));
    }

    // for reports
    @Operation(summary = "Summarize expenses by ledger category",
        description = "Returns expense totals by ledger category for the specified apartment and month.")
    @GetMapping("/summary/ledger-category")
    public ResponseEntity<List<LedgerCategoryExpenseSummary>> getSummaryByPaymentMode(
        @Parameter(description = "ID of the apartment.") @PathVariable Long apartmentId,
        @Parameter(description = "Calendar year of the expense period.") @RequestParam Short year,
        @Parameter(description = "Month of the expense period, from 1 to 12.")
        @Min(1) @Max(12)
        @RequestParam Short month) {

        return ResponseEntity.ok(expenseService.getExpenseSummaryByCategory(apartmentId,year,month));
    }

}