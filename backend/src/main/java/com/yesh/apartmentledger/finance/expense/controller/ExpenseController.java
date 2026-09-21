package com.yesh.apartmentledger.finance.expense.controller;

import com.yesh.apartmentledger.core.enums.ApprovalStatusEnum;
import com.yesh.apartmentledger.finance.expense.dto.*;
import com.yesh.apartmentledger.finance.expense.service.ExpenseService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/expenses")
@AllArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping("/drafts")
    public ResponseEntity<String> submitDrafts(
            @RequestParam Long apartmentId,
            @RequestBody List<ExpenseDraftRequest> requests) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(expenseService.saveExpenseDrafts(apartmentId, requests));
    }

    @GetMapping("/drafts/pending")
    public ResponseEntity<List<ExpensePendingDraftResponse>> getPendingDrafts(
            @RequestParam Long apartmentId) {
        return ResponseEntity.ok(expenseService.getPendingDrafts(apartmentId));
    }

    @PostMapping("/drafts/approve")
    public ResponseEntity<String> approveDrafts(
            @RequestParam Long apartmentId,
            @RequestBody ExpenseApprovalRequest request) {
        return ResponseEntity.ok(expenseService.approveDrafts(apartmentId, request));
    }
}