package com.yesh.apartmentledger.finance.expense.service;

import com.yesh.apartmentledger.core.apartment.entity.Apartment;
import com.yesh.apartmentledger.core.apartment.repository.ApartmentRepository;
import com.yesh.apartmentledger.core.enums.ApprovalStatusEnum;
import com.yesh.apartmentledger.core.user.entity.AppUser;
import com.yesh.apartmentledger.core.user.repository.AppUserRepository;
import com.yesh.apartmentledger.core.validation.LedgerValidationUtil;
import com.yesh.apartmentledger.exception.ResourceNotFoundException;
import com.yesh.apartmentledger.finance.expense.dto.ExpenseApprovalRequest;
import com.yesh.apartmentledger.finance.expense.dto.ExpenseDraftRequest;
import com.yesh.apartmentledger.finance.expense.dto.ExpensePendingDraftResponse;
import com.yesh.apartmentledger.finance.expense.entity.Expense;
import com.yesh.apartmentledger.finance.expense.entity.ExpenseDraft;
import com.yesh.apartmentledger.finance.expense.repository.ExpenseDraftRepository;
import com.yesh.apartmentledger.finance.expense.repository.ExpenseRepository;
import com.yesh.apartmentledger.master.approvalstatus.entity.ApprovalStatus;
import com.yesh.apartmentledger.master.approvalstatus.repository.ApprovalStatusRepository;
import com.yesh.apartmentledger.master.ledgercategory.entity.LedgerCategory;
import com.yesh.apartmentledger.master.ledgercategory.repository.LedgerCategoryRepository;
import com.yesh.apartmentledger.master.paymentmode.entity.PaymentMode;
import com.yesh.apartmentledger.master.paymentmode.repository.PaymentModeRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class ExpenseService {

    private final ExpenseDraftRepository expenseDraftRepository;
    private final ExpenseRepository expenseRepository;
    private final ApartmentRepository apartmentRepository;
    private final LedgerValidationUtil ledgerValidationUtil;
    private final PaymentModeRepository paymentModeRepository;
    private final AppUserRepository appUserRepository;
    private final ApprovalStatusRepository approvalStatusRepository;
    private final LedgerCategoryRepository ledgerCategoryRepository;
    // ==========================================
    // MAKER: SUBMIT NEW EXPENSE DRAFTS
    // ==========================================
    @Transactional
    public String saveExpenseDrafts(Long apartmentId, List<ExpenseDraftRequest> requests) {
        Apartment apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new RuntimeException("Apartment not found"));

        ApprovalStatus pendingStatus = approvalStatusRepository.findByStatusNameIgnoreCase (ApprovalStatusEnum.PENDING.name())
                .orElseThrow(()-> new ResourceNotFoundException("Approval status PENDING not found in master."));

        List<ExpenseDraft> drafts = new ArrayList<>();

        for (ExpenseDraftRequest request : requests) {
            // 1. Vault Door Check! Blocks if Go-Live is invalid or Month is closed
            ledgerValidationUtil.validateBillingPeriod(apartment, request.txnYear(), request.txnMonth());
            AppUser maker = appUserRepository.findById(request.makerId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            LedgerCategory ledgerCategory = ledgerCategoryRepository.findById(request.ledgerCategoryId())
                    .orElseThrow(()-> new ResourceNotFoundException("Ledger category not found for [ "+ request.ledgerCategoryId() + " ]"));

            PaymentMode paymentMode = paymentModeRepository.findById(request.paymentModeId())
                    .orElseThrow(()-> new ResourceNotFoundException("Payment mode not found for [ "+ request.paymentModeId() + " ]"));
            ApprovalStatus approvalStatus = approvalStatusRepository.findByStatusNameIgnoreCase(ApprovalStatusEnum.PENDING.name())
                    .orElseThrow(()-> new ResourceNotFoundException("Approval Status PENDING not found in database."));
            ExpenseDraft draft = new ExpenseDraft();

            draft.setApartment(apartment);
            draft.setId(maker.getId());
            draft.setLedgerCategory(ledgerCategory);
            draft.setPaymentMode(paymentMode);
            draft.setApprovalStatus(approvalStatus);
            // ... map category, payment mode, etc. using IDs from request ...
            draft.setTransactionDate(request.transactionDate());
            draft.setTxnYear(request.txnYear());
            draft.setTxnMonth(request.txnMonth());
            draft.setAmount(request.amount());
            draft.setReferenceNumber(request.referenceNumber());
            draft.setRemarks(request.remarks());
            draft.setApprovalStatus(pendingStatus);
            draft.setCreatedBy(maker);

            drafts.add(draft);
        }

        expenseDraftRepository.saveAll(drafts);
        return drafts.size() + " expense draft(s) saved and sent for approval.";
    }

    // ==========================================
    // CHECKER: FETCH PENDING EXPENSES
    // ==========================================
    @Transactional(readOnly = true)
    public List<ExpensePendingDraftResponse> getPendingDrafts(Long apartmentId) {
        String pendingStatus =  ApprovalStatusEnum.PENDING.name();
        List<ExpenseDraft> drafts = expenseDraftRepository.findByApartmentIdAndApprovalStatus_StatusName(apartmentId, pendingStatus);

        return drafts.stream().map(d -> new ExpensePendingDraftResponse(
                d.getId(),
                d.getLedgerCategory().getCategoryName(),
                d.getTransactionDate(),
                d.getTxnYear(),
                d.getTxnMonth(),
                d.getAmount(),
                d.getPaymentMode().getPaymentModeName(),
                d.getReferenceNumber(),
                d.getRemarks(),
                d.getCreatedBy().getDisplayName()
        )).toList();
    }

    // ==========================================
    // CHECKER: APPROVE EXPENSES (Final Commit)
    // ==========================================
    @Transactional
    public String approveDrafts(Long apartmentId, ExpenseApprovalRequest request) {

        AppUser approver = appUserRepository.findById(request.userId())
                .orElseThrow(()-> new ResourceNotFoundException("User not found for id [" + request.draftIds() + "]")); // Hardcode Checker ID 2

        ApprovalStatus approvedStatus = approvalStatusRepository.findByStatusNameIgnoreCase(ApprovalStatusEnum.APPROVED.name())
                .orElseThrow(()-> new ResourceNotFoundException("Approved status not found in database"));

        List<Expense> finalLedgerEntries = new ArrayList<>();
        List<ExpenseDraft> draftsToUpdate = new ArrayList<>();

        for (Long draftId : request.draftIds()) {
            ExpenseDraft draft = expenseDraftRepository.findById(draftId)
                    .orElseThrow(() -> new ResourceNotFoundException("Draft not found: " + draftId));

            // Vault Door Check AGAIN (in case the month was closed while this was pending!)
            ledgerValidationUtil.validateBillingPeriod(draft.getApartment(), draft.getTxnYear(), draft.getTxnMonth());

            // 1. Update Draft Status
            draft.setApprovalStatus(approvedStatus);
            draftsToUpdate.add(draft);

            // 2. Create Final Immutable Ledger Entry
            Expense expense = new Expense();
            expense.setApartment(draft.getApartment());
            expense.setLedgerCategory(draft.getLedgerCategory());
            expense.setTransactionDate(draft.getTransactionDate());
            expense.setTxnYear(draft.getTxnYear());
            expense.setTxnMonth(draft.getTxnMonth());
            expense.setAmount(draft.getAmount());
            expense.setPaymentMode(draft.getPaymentMode());
            expense.setReferenceNumber(draft.getReferenceNumber());
            expense.setRemarks(draft.getRemarks());
            expense.setCreatedBy(draft.getCreatedBy());
            expense.setApprovedBy(approver);

            finalLedgerEntries.add(expense);
        }

        expenseRepository.saveAll(finalLedgerEntries);
        expenseDraftRepository.saveAll(draftsToUpdate);

        return request.draftIds().size() + " expense(s) approved and posted to the ledger.";
    }
}