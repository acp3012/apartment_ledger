package com.yesh.apartmentledger.finance.expense.service;

import com.yesh.apartmentledger.core.apartment.entity.Apartment;
import com.yesh.apartmentledger.core.apartment.repository.ApartmentRepository;
import com.yesh.apartmentledger.core.enums.ApprovalStatusEnum;
import com.yesh.apartmentledger.core.user.entity.AppUser;
import com.yesh.apartmentledger.core.user.repository.AppUserRepository;
import com.yesh.apartmentledger.core.validation.LedgerValidationUtil;
import com.yesh.apartmentledger.core.validation.MasterEntityValidator;
import com.yesh.apartmentledger.exception.BadRequestException;
import com.yesh.apartmentledger.exception.ResourceNotFoundException;
import com.yesh.apartmentledger.finance.expense.dto.*;
import com.yesh.apartmentledger.finance.expense.entity.Expense;
import com.yesh.apartmentledger.finance.expense.entity.ExpenseDraft;
import com.yesh.apartmentledger.finance.expense.repository.ExpenseDraftRepository;
import com.yesh.apartmentledger.finance.expense.repository.ExpenseRepository;
import com.yesh.apartmentledger.finance.income.dto.PaymentModeIncomeSummary;
import com.yesh.apartmentledger.master.approvalstatus.entity.ApprovalStatus;
import com.yesh.apartmentledger.master.approvalstatus.repository.ApprovalStatusRepository;
import com.yesh.apartmentledger.master.cache.LookupCacheService;
import com.yesh.apartmentledger.master.ledgercategory.entity.LedgerCategory;
import com.yesh.apartmentledger.master.ledgercategory.repository.LedgerCategoryRepository;
import lombok.AllArgsConstructor;
import org.jboss.logging.Logger;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class ExpenseService {

    private final ExpenseDraftRepository expenseDraftRepository;
    private final ExpenseRepository expenseRepository;
    private final ApartmentRepository apartmentRepository;
    private final LedgerValidationUtil ledgerValidationUtil;
    private final MasterEntityValidator entityValidator;
    private final AppUserRepository appUserRepository;
    private final ApprovalStatusRepository approvalStatusRepository;
    private final LedgerCategoryRepository ledgerCategoryRepository;
    private final LookupCacheService lookupCacheService;
    private final String DEBIT_CODE = "DR";

    // ==========================================
    // MAKER: SUBMIT NEW EXPENSE DRAFTS
    // ==========================================
    @Transactional
    public void createExpenseDraft(Long apartmentId, ExpenseDraftRequest request) {
        Apartment apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Apartment not found"));
        // IMPORTANT Do not allow out of cycle date.
        ledgerValidationUtil.validateBillingPeriod(apartment,request.transactionDate());
        ApprovalStatus pendingStatus =  lookupCacheService.getApprovalStatusById(ApprovalStatusEnum.PENDING.getValue());

        List<ExpenseDraft> drafts = new ArrayList<>();

        var result = entityValidator.validateAndGetEntities(apartmentId,
                request.ledgerCategoryId(),
                request.paymentModeId(),
                request.makerId()
                );

       entityValidator.throwErrorIfLedgerCategoryCodeNotMatch(result.ledgerCategory().getTransactionType(),DEBIT_CODE);

        ExpenseDraft draft = createExpenseDraft(request, apartment, result, pendingStatus);

        expenseDraftRepository.save(draft);
    }
    //==============================================
    // MAKER: Update draft for modification
    //==============================================
    @Transactional
    public void updateDraft(Long apartmentId,Long expenseDraftId, ExpenseDraftRequest payload) {

        // 1. Transaction period validation
        ledgerValidationUtil.validateBillingPeriod(apartmentId,payload.transactionDate());
        // 2. validate the draft id
        var expenseDraft = validateDraftWithApartment(apartmentId,expenseDraftId);

        // Business validation
        var category =  ledgerCategoryRepository.findById(payload.ledgerCategoryId())
                .orElseThrow(()-> new BadRequestException("Invalid Ledger Category code"));

        // Make sure the transaction type is Debit
        entityValidator.throwErrorIfLedgerCategoryCodeNotMatch(category.getTransactionType(),DEBIT_CODE);

        if (!expenseDraft.getApprovalStatus().getStatusName().equals(ApprovalStatusEnum.PENDING.name())) {
            throw new BadRequestException("Only draft with PENDING status can be updated.");
        }
        var entities =  entityValidator.validateAndGetEntities(
                apartmentId,
                payload.ledgerCategoryId(),
                payload.paymentModeId(),
                payload.makerId()
        );

        expenseDraft.setPaymentMode(entities.paymentMode());
        expenseDraft.setLedgerCategory(entities.ledgerCategory());
        expenseDraft.setAmount(payload.amount());
        expenseDraft.setReferenceNumber(payload.referenceNumber());
        expenseDraft.setRemarks(payload.remarks());
        // audit
        expenseDraft.setUpdated_by(entities.user());
        expenseDraft.setUpdatedDate(LocalDateTime.now());

        expenseDraftRepository.save(expenseDraft);
        
    }



    // ==========================================
    // CHECKER: FETCH PENDING EXPENSES
    // ==========================================
    @Transactional(readOnly = true)
    public List<ExpenseDraftResponse> getPendingDrafts(Long apartmentId) {
        String pendingStatus =  ApprovalStatusEnum.PENDING.name();

        List<ExpenseDraft> drafts = expenseDraftRepository.findByApartmentIdAndApprovalStatus_StatusName(apartmentId, pendingStatus);

        return drafts.stream().map(ExpenseService::toExpenseDraftResponse).toList();
    }
    //=============================================
    // GET draft by id
    //=============================================
    @Transactional(readOnly = true)
    public ExpenseDraftResponse getDraftById(Long apartmentId, Long draftId) {
        ExpenseDraft expenseDraft = validateDraftWithApartment(apartmentId, draftId);
        return toExpenseDraftResponse(expenseDraft);
    }   

       
    // ==========================================
    // CHECKER: APPROVE EXPENSES (Final Commit)
    // ==========================================
    @Transactional
    public String approveDrafts(Long apartmentId, ExpenseApprovalRequest request) {
        return draftApprovalDecision(apartmentId, request, ApprovalStatusEnum.APPROVED);
    }

    @Transactional
    public String rejectDrafts(Long apartmentId, ExpenseApprovalRequest request) {
        return draftApprovalDecision(apartmentId, request, ApprovalStatusEnum.REJECTED);
    }


    //=============================================
    // GET Calls
    //=============================================
    @Transactional(readOnly = true)
    public List<ExpenseDraftResponse> getDraftsByApprovalStatusYearAndMonth(
            Long apartmentId,
            String approvalStatus,
            Short year,
            Short month) {
        if (month != null && (month < 1 || month > 12)) {
            throw new BadRequestException("Invalid month [ " + month + " ]");
        }
        // approval status not given, pull only pending drafts
        List<ExpenseDraft> drafts = expenseDraftRepository.findByApartmentIdAndApprovalStatus_StatusName(apartmentId,
                approvalStatus != null ? approvalStatus.toUpperCase() : ApprovalStatusEnum.PENDING.name());

        return drafts.stream()
                .filter(d -> year == null || d.getTxnYear().equals(year))
                .filter(d -> month == null || d.getTxnMonth().equals(month))
            .map(ExpenseService::toExpenseDraftResponse)
            .toList();
    }
    //==============================================
    // GET Expense all (optional filters)
    //===============================================
    @Transactional(readOnly = true)
    public List<ExpenseResponse> getExpenseByApartmentYearAndMonth(Long apartmentId, Short year, Short month) {
        if (month != null && (month < 1 || month > 12)) {
            throw new BadRequestException("Invalid month [ " + month + " ]");
        }
        var expenses =  expenseRepository.findExpensesByApartmentAndPeriod(apartmentId,year,month).stream().toList();
        return  expenses.stream().map(ExpenseService::toExpenseResponse).toList();
    }


    //==================================================
    // PRIVATE METHODS
    //===================================================
    private static ExpenseDraftResponse toExpenseDraftResponse(ExpenseDraft draft) {
        return new ExpenseDraftResponse(
                draft.getId(),
                draft.getTransactionDate(),
                draft.getLedgerCategory().getCategoryName(),
                draft.getAmount(),
                draft.getPaymentMode().getPaymentModeName(),
                draft.getReferenceNumber(),
                draft.getRemarks(),
                draft.getApprovalStatus().getStatusName(),
                draft.getCreatedDate(),
                draft.getCreatedBy().getDisplayName(),
                draft.getApprovedDate()== null? null : draft.getApprovedDate(),
                draft.getApprovedBy() == null? null: draft.getApprovedBy().getDisplayName()
        );
    }
    private static ExpenseResponse  toExpenseResponse(Expense expense){
        return new ExpenseResponse(
                expense.getId(),
                expense.getTransactionDate(),
                expense.getLedgerCategory().getCategoryName(),
                expense.getAmount(),
                expense.getPaymentMode().getPaymentModeName(),
                expense.getReferenceNumber(),
                expense.getRemarks(),
                expense.getCreatedDate(),
                expense.getCreatedBy().getDisplayName(),
                expense.getApprovedDate(),
                expense.getApprovedBy().getDisplayName()
        );
    }
    private static @NonNull Expense createExpense(ExpenseDraft draft, AppUser approver) {
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
        expense.setCreatedDate(draft.getCreatedDate());
        expense.setApprovedBy(approver);
        expense.setApprovedDate(LocalDateTime.now());
        return expense;
    }
    private static @NonNull ExpenseDraft createExpenseDraft(ExpenseDraftRequest request,
                                                            Apartment apartment,
                                                            MasterEntityValidator.EntityResult result,
                                                            ApprovalStatus pendingStatus) {
        ExpenseDraft draft = new ExpenseDraft();
        draft.setApartment(apartment);
        draft.setLedgerCategory(result.ledgerCategory());
        draft.setPaymentMode(result.paymentMode());
        draft.setTransactionDate(request.transactionDate());
        draft.setAmount(request.amount());
        draft.setReferenceNumber(request.referenceNumber());
        draft.setRemarks(request.remarks());
        draft.setApprovalStatus(pendingStatus);
        draft.setCreatedBy(result.user());
        return draft;
    }
    private ExpenseDraft validateDraftWithApartment(Long apartmentId,Long draftId){
        ExpenseDraft draft = expenseDraftRepository.findById(draftId)
                .orElseThrow(() -> new ResourceNotFoundException("Draft not found: " + draftId));

        if (!draft.getApartment().getId().equals(apartmentId)) {
            throw new BadRequestException("Draft does not belong to apartment " + apartmentId);
            }
        return draft;
    }
    //=====================================================
    // Common method to Reject or Approve
    //======================================================
    @Transactional
    private String draftApprovalDecision(Long apartmentId, ExpenseApprovalRequest request, ApprovalStatusEnum approvalStatusEnum) {

        AppUser approver = appUserRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found for id [" + request.userId() + "]"));

        ApprovalStatus targetStatus = approvalStatusRepository.findByStatusNameIgnoreCase(approvalStatusEnum.name())
                .orElseThrow(() -> new ResourceNotFoundException("Approval status not found in database"));

        List<Expense> finalLedgerEntries = new ArrayList<>();
        List<ExpenseDraft> draftsToUpdate = new ArrayList<>();

        for (Long draftId : request.draftIds()) {

            ExpenseDraft draft = validateDraftWithApartment(apartmentId,draftId);
            ledgerValidationUtil.validateBillingPeriod(draft.getApartment(), draft.getTxnYear(), draft.getTxnMonth());

            draft.setApprovalStatus(targetStatus);
            draft.setApprovedBy(approver);
            draft.setApprovedDate(LocalDateTime.now());
            draft.setApprovalComments(request.rejectReason());
            draftsToUpdate.add(draft);

            if (ApprovalStatusEnum.APPROVED == approvalStatusEnum) {
                finalLedgerEntries.add(createExpense(draft, approver));
            }
        }
        // Push data to expense table  if approved otherwise just update expense_draft
        if (!finalLedgerEntries.isEmpty()) {
            expenseRepository.saveAll(finalLedgerEntries);
        }
        expenseDraftRepository.saveAll(draftsToUpdate);

        return request.draftIds().size() + " expense(s) " + approvalStatusEnum.name().toLowerCase() + " and processed.";
    }
    // Reports
    // Summary by Ledger Category
    public List<LedgerCategoryExpenseSummary> getExpenseSummaryByCategory(Long apartmentId, Short year, Short month) {
            return expenseRepository.getExpenseSummaryByCategory(apartmentId,year, month);
    }
}