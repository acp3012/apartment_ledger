package com.yesh.apartmentledger.finance.income.service;

import com.yesh.apartmentledger.core.apartment.entity.Apartment;
import com.yesh.apartmentledger.core.apartment.repository.ApartmentRepository;
import com.yesh.apartmentledger.core.enums.ApprovalStatusEnum;
import com.yesh.apartmentledger.core.enums.TransactionTypeEnum;
import com.yesh.apartmentledger.core.flat.entity.Flat;
import com.yesh.apartmentledger.core.flat.repository.FlatRepository;
import com.yesh.apartmentledger.core.user.entity.AppUser;
import com.yesh.apartmentledger.core.user.repository.AppUserRepository;
import com.yesh.apartmentledger.core.validation.LedgerValidationUtil;
import com.yesh.apartmentledger.exception.BadRequestException;
import com.yesh.apartmentledger.exception.ResourceNotFoundException;
import com.yesh.apartmentledger.finance.income.dto.*;
import com.yesh.apartmentledger.finance.income.entity.Income;
import com.yesh.apartmentledger.finance.income.entity.IncomeDraft;
import com.yesh.apartmentledger.finance.income.repository.IncomeDraftRepository;
import com.yesh.apartmentledger.finance.income.repository.IncomeRepository;
import com.yesh.apartmentledger.master.approvalstatus.entity.ApprovalStatus;
import com.yesh.apartmentledger.master.approvalstatus.repository.ApprovalStatusRepository;
import com.yesh.apartmentledger.master.ledgercategory.entity.LedgerCategory;
import com.yesh.apartmentledger.master.ledgercategory.repository.LedgerCategoryRepository;
import com.yesh.apartmentledger.master.paymentmode.entity.PaymentMode;
import com.yesh.apartmentledger.master.paymentmode.repository.PaymentModeRepository;
import com.yesh.apartmentledger.security.model.CustomUserDetails;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final AppUserRepository appUserRepository;
    private final LedgerValidationUtil legerValidationUtil;

    @Transactional
    public void createDraft(Long apartmentId, IncomeDraftRequest request) {

        // 1. Fetch Multi-tenant context
        Apartment apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Apartment not found"));

        // 2. Fetch the Maker
        AppUser maker = appUserRepository.findById(request.makerId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 3. Fetch Master Data

        LedgerCategory category = ledgerCategoryRepository.findById(request.ledgerCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if(!category.getTransactionType().equalsIgnoreCase(TransactionTypeEnum.CR.name())){
            throw new BadRequestException("Ledger Category must be a Credit");
        }
        PaymentMode paymentMode = paymentModeRepository.findById(request.paymentModeId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment Mode not found"));

        // 4. Fetch the initial 'PENDING' approval status.
        // Assuming 'PENDING' is a valid status_code in your master table.
        ApprovalStatus pendingStatus = approvalStatusRepository.findByStatusNameIgnoreCase(ApprovalStatusEnum.PENDING.name())
                .orElseThrow(() -> new ResourceNotFoundException("Default approval status not found"));
        //var statusId = ApprovalStatusEnum.PENDING.getValue();
        // 5. Build the Draft Entity
        IncomeDraft draft = new IncomeDraft();
        draft.setApartment(apartment);
        draft.setCreatedBy(maker);
        draft.setLedgerCategory(category);
        draft.setPaymentMode(paymentMode);
        draft.setApprovalStatus(pendingStatus);

        // Map the basic fields from the Request Record
        draft.setAmount(request.amount());
        draft.setReferenceNumber(request.referenceNumber());
        draft.setRemarks(request.remarks());

        // Handle the date parsing from string (YYYY-MM-DD)
       LocalDate txnDate = request.transactionDate();
        draft.setTransactionDate(txnDate);

        // 6. Conditionally set the Flat (Only for Maintenance Tab)
        if (request.flatId() != null) {
            Flat flat = flatRepository.findById(request.flatId())
                    .orElseThrow(() -> new ResourceNotFoundException("Flat not found"));

            // Security check: Ensure the flat actually belongs to this apartment!
            if (!flat.getApartment().getId().equals(apartmentId)) {
                throw new IllegalStateException("Flat does not belong to the given apartment");
            }

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
        income.setLedgerCategory(ledgerCategoryRepository.getReferenceById(payload.ledgerCategoryId()));
        income.setPaymentMode(paymentModeRepository.getReferenceById(payload.paymentModeId()));
        income.setApartment(apartmentRepository.getReferenceById(apartmentId));
        // Safely handle flat mapping (in case it's a general apartment income without a flat)
        if (payload.flatId() != null) {
            income.setFlat(flatRepository.getReferenceById(payload.flatId()));
        } else {
            income.setFlat(null);
        }
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
         if(month != null && ( month < 1 || month > 12)) {
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

        var processTime = LocalDateTime.now();

        AppUser approver = appUserRepository.findById(request.approverId())
                .orElseThrow(() -> new ResourceNotFoundException("Approver user not found"));

        boolean isApproved = ApprovalStatusEnum.APPROVED == approvalStatusEnum;
        ApprovalStatus approvedStatus = approvalStatusRepository.findByStatusNameIgnoreCase(approvalStatusEnum.name())
                .orElseThrow(()-> new ResourceNotFoundException("Approval status not found"));

        List<Income> finalIncomeEntries = new ArrayList<>();
        List<IncomeDraft> draftsToUpdate = new ArrayList<>();

        for (Long draftId : request.draftIds()) {
            IncomeDraft draft = incomeDraftRepository.findById(draftId)
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

   
    // Helper to safely extract the Apartment ID from the JWT Context

    private Long getCurrentApartmentId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails user) {
            return user.getApartmentId();
        }
        throw new IllegalStateException("Security context missing or invalid.");
    }

    /**
     * Helper to get the logged-in User entity
     */
    private AppUser getCurrentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            return appUserRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalStateException("User not found"));
        }
        throw new IllegalStateException("User not found in context");
    }


    private Long getApprovalStatusEnum(String statusName){
        Long statusId;
        try {
            statusId =  ApprovalStatusEnum.getValueByText(statusName);
        }
        catch(Exception e){
            throw new ResourceNotFoundException("Approval status [" + statusName + "] does not exists");
        }
        return statusId;

    }
}