package com.yesh.apartmentledger.finance.income.service;

import com.yesh.apartmentledger.core.validation.MonthRangeValidator;
import com.yesh.apartmentledger.core.apartment.entity.Apartment;
import com.yesh.apartmentledger.core.apartment.repository.ApartmentRepository;
import com.yesh.apartmentledger.core.enums.ApprovalStatusEnum;
import com.yesh.apartmentledger.core.enums.TransactionTypeEnum;
import com.yesh.apartmentledger.core.flat.entity.Flat;
import com.yesh.apartmentledger.core.flat.repository.FlatRepository;
import com.yesh.apartmentledger.core.user.entity.AppUser;
import com.yesh.apartmentledger.exception.BadRequestException;
import com.yesh.apartmentledger.exception.ResourceNotFoundException;
import com.yesh.apartmentledger.finance.income.dto.*;
import com.yesh.apartmentledger.finance.income.entity.Income;
import com.yesh.apartmentledger.finance.income.entity.IncomeDraft;
import com.yesh.apartmentledger.finance.income.repository.IncomeDraftRepository;
import com.yesh.apartmentledger.finance.income.repository.IncomeRepository;
import com.yesh.apartmentledger.master.approvalstatus.entity.ApprovalStatus;
import com.yesh.apartmentledger.master.approvalstatus.repository.ApprovalStatusRepository;
import com.yesh.apartmentledger.master.cache.LookupCacheService;
import com.yesh.apartmentledger.master.ledgercategory.entity.LedgerCategory;
import com.yesh.apartmentledger.master.ledgercategory.repository.LedgerCategoryRepository;
import com.yesh.apartmentledger.master.paymentmode.entity.PaymentMode;
import com.yesh.apartmentledger.master.paymentmode.repository.PaymentModeRepository;
import com.yesh.apartmentledger.security.service.CurrentUserService;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class IncomeDraftService {

    private final IncomeDraftRepository incomeDraftRepository;
    private final IncomeRepository incomeRepository;
    private final ApartmentRepository apartmentRepository;
    private final FlatRepository flatRepository;

    private final PaymentModeRepository paymentModeRepository;
    private final LedgerCategoryRepository ledgerCategoryRepository;
    private final ApprovalStatusRepository approvalStatusRepository;
    private final LookupCacheService lookupCacheService;
    private final CurrentUserService currentUserService;

    @Transactional
    public void createDraft(Long apartmentId, IncomeDraftRequest request) {
        currentUserService.requireApartmentAccess(apartmentId);

        // 1. Fetch Multi-tenant context
        Apartment apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Apartment not found"));

        // 2. Fetch the Maker
        AppUser maker = currentUserService.getCurrentUser();

        // 3. Fetch Master Data

        LedgerCategory category = lookupCacheService.getLedgerCategoryById(request.ledgerCategoryId());
        if (category == null) {
            throw new ResourceNotFoundException("Category not found");
        }

        if(!category.getTransactionType().equalsIgnoreCase(TransactionTypeEnum.CR.name())){
            throw new BadRequestException("Ledger Category must be a Credit");
        }
        PaymentMode paymentMode = lookupCacheService.getPaymentModeById(request.paymentModeId());
        if (paymentMode == null) {
            throw new ResourceNotFoundException("Payment Mode not found");
        }

        // 4. Fetch the initial 'PENDING' approval status.
        // Assuming 'PENDING' is a valid status_code in your master table.
        ApprovalStatus cachedPendingStatus = lookupCacheService.getApprovalStatusByName(ApprovalStatusEnum.PENDING.name());
        if (cachedPendingStatus == null) {
            throw new ResourceNotFoundException("Default approval status not found");
        }
        //var statusId = ApprovalStatusEnum.PENDING.getValue();
        // 5. Build the Draft Entity
        IncomeDraft draft = new IncomeDraft();
        draft.setApartment(apartment);
        draft.setCreatedBy(maker);
        draft.setLedgerCategory(ledgerCategoryRepository.getReferenceById(category.getId()));
        draft.setPaymentMode(paymentModeRepository.getReferenceById(paymentMode.getId()));
        draft.setApprovalStatus(approvalStatusRepository.getReferenceById(cachedPendingStatus.getId()));

        // Map the basic fields from the Request Record
        draft.setAmount(request.amount());
        draft.setReferenceNumber(request.referenceNumber());
        draft.setRemarks(request.remarks());

        // Handle the date parsing from string (YYYY-MM-DD)
       LocalDate txnDate = request.transactionDate();
        draft.setTransactionDate(txnDate);

        // 6. Conditionally set the Flat (Only for Maintenance Tab)
        if (request.flatId() != null) {
            Flat flat = flatRepository.findByIdAndApartmentId(request.flatId(), apartmentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Flat not found"));

            draft.setFlat(flat);
        }

        // 7. Save to PostgreSQL
        incomeDraftRepository.save(draft);
    }
    //==============================================================
    // UPDATE DRAFT BY ID
    //=============================================================
    @Transactional
    public void updateDraft(Long apartmentId, Long draftId, IncomeDraftRequest payload) {
        currentUserService.requireApartmentAccess(apartmentId);
        // find the draftId
        // 1. Fetch the draft and ensure it belongs to the specified apartment
        var income = incomeDraftRepository.findByIdAndApartmentId(draftId, apartmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Income draft id [" + draftId + "] does not exist."));

        // 2. Business Rule: Prevent editing of locked/approved drafts

        if (income.getApprovalStatus().toString().equals(ApprovalStatusEnum.APPROVED.name())) {
            throw new BadRequestException("Cannot update an income draft that has already been approved.");
        }
        // 3. Update basic fields

        income.setTransactionDate(payload.transactionDate());
        income.setAmount(payload.amount());
        income.setReferenceNumber(payload.referenceNumber());
        // 4. Update relationships using fast proxies
        LedgerCategory category = lookupCacheService.getLedgerCategoryById(payload.ledgerCategoryId());
        if (category == null) {
            throw new ResourceNotFoundException("Category not found");
        }
        PaymentMode paymentMode = lookupCacheService.getPaymentModeById(payload.paymentModeId());
        if (paymentMode == null) {
            throw new ResourceNotFoundException("Payment Mode not found");
        }
        income.setLedgerCategory(ledgerCategoryRepository.getReferenceById(category.getId()));
        income.setPaymentMode(paymentModeRepository.getReferenceById(paymentMode.getId()));
        income.setApartment(apartmentRepository.getReferenceById(apartmentId));
        // Safely handle flat mapping (in case it's a general apartment income without a flat)
        if (payload.flatId() != null) {
            Flat flat = flatRepository.findByIdAndApartmentId(payload.flatId(), apartmentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Flat not found"));
            income.setFlat(flat);
        } else {
            income.setFlat(null);
        }
        income.setUpdatedBy(currentUserService.getCurrentUser());
        incomeDraftRepository.save(income);
    }


    // ==========================================
    // FETCH  DRAFTS BY APPROVAL STATUS
    // ==========================================

    @Transactional(readOnly = true)
    public List<IncomeDraftResponse> getDraftsByApprovalStatusYearAndMonth(
            Long apartmentId,
            @Nullable String approvalStatus,
            @Nullable Short year,
            @Nullable Short month
            ) {
            currentUserService.requireApartmentAccess(apartmentId);
         if(!MonthRangeValidator.isValidMonth(month)) {
             throw new BadRequestException("Invalid month [ " + month + " ]");
         }
         Long statusId =  null ;
         if(approvalStatus != null){
             statusId = ApprovalStatusEnum.getValueByText (approvalStatus);
         }

         List<IncomeDraft> pendingDrafts = incomeDraftRepository.findDynamicDrafts(apartmentId,statusId,year,month);

        return pendingDrafts.stream().map(this::mapToResponse).toList();

    }

    // ==========================================
    // APPROVE  OR REJECT DRAFTS (The Master Commit)
    // ==========================================

    @Transactional
    public String draftApprovalDecision(Long apartmentId, DraftApprovalRequest request, ApprovalStatusEnum approvalStatusEnum) {
        currentUserService.requireApartmentAccess(apartmentId);

        var processTime = LocalDateTime.now();

        AppUser approver = currentUserService.getCurrentUser();

        boolean isApproved = ApprovalStatusEnum.APPROVED == approvalStatusEnum;
        ApprovalStatus cachedStatus = lookupCacheService.getApprovalStatusByName(approvalStatusEnum.name());
        if (cachedStatus == null) {
            throw new ResourceNotFoundException("Approval status not found");
        }
        ApprovalStatus approvedStatus = approvalStatusRepository.getReferenceById(cachedStatus.getId());

        List<Income> finalIncomeEntries = new ArrayList<>();
        List<IncomeDraft> draftsToUpdate = new ArrayList<>();

        for (Long draftId : request.draftIds()) {
                IncomeDraft draft = incomeDraftRepository.findByIdAndApartmentId(draftId, apartmentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Draft not found: " + draftId));

            // 1. Update the Draft Audit Trail
            draft.setApprovalStatus(approvedStatus);
            draft.setUpdatedDate(processTime);
            draft.setUpdatedBy(approver);
            draft.setApprovalComments(request.comments());
            draft.setApprovedBy(approver);
            draft.setApproveDate(processTime);
            draftsToUpdate.add(draft);
            // 2. Create the Immutable Ledger Entry
           // MaintenancePayment payment = new MaintenancePayment();
            Income income = createIncome(draft, approver,processTime);

            if(isApproved) {
                finalIncomeEntries.add(income);
            }
        }

        // Only approved to go to Income
        if (!finalIncomeEntries.isEmpty()){
            incomeRepository.saveAll(finalIncomeEntries);
        }

        incomeDraftRepository.saveAll(draftsToUpdate);
        return request.draftIds().size() + " draft(s) successfully approved and posted to the ledger.";
    }


    // ====================================================================
    // Private methods
    // =====================================================================

    private static @NonNull Income createIncome(IncomeDraft draft, AppUser approver, LocalDateTime approvedTime) {
        Income income = new Income();
        income.setApartment(draft.getApartment());
        income.setFlat(draft.getFlat());
        income.setLedgerCategory(draft.getLedgerCategory());
        income.setTransactionDate(draft.getTransactionDate());
        income.setAmount(draft.getAmount());
        income.setPaymentMode(draft.getPaymentMode());
        income.setReferenceNo(draft.getReferenceNumber());
        income.setRemarks(draft.getRemarks());

        // Maker-Checker Audit Link
        income.setCreatedBy(draft.getCreatedBy());
        income.setApprovedBy(approver);
        income.setApprovedDate(approvedTime);
        return income;
    }
    // Helper method to convert Entity to Response DTO
    private IncomeDraftResponse mapToResponse(IncomeDraft entity) {
        return new IncomeDraftResponse(
                entity.getId(),
                entity.getFlat() != null ? entity.getFlat().getId() : null,
                entity.getFlat() != null ? entity.getFlat().getFlatNumber() : null,
                entity.getTransactionDate(),
                entity.getAmount(),
                entity.getPaymentMode().getId(),
                entity.getPaymentMode().getPaymentModeName(),
                entity.getReferenceNumber(),
                entity.getRemarks(),
                entity.getApprovalStatus().getStatusName(),
                entity.getCreatedBy().getDisplayName(),
                entity.getLedgerCategory().getId()
        );
    }
}