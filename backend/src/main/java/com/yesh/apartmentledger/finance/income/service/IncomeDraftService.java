package com.yesh.apartmentledger.finance.income.service;

import com.yesh.apartmentledger.core.apartment.entity.Apartment;
import com.yesh.apartmentledger.core.apartment.repository.ApartmentRepository;
import com.yesh.apartmentledger.core.enums.ApprovalStatusEnum;
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
import com.yesh.apartmentledger.finance.maintenance.entity.MaintenanceAdvance;
import com.yesh.apartmentledger.finance.maintenance.entity.MaintenancePayment;
import com.yesh.apartmentledger.finance.maintenance.repository.MaintenanceAdvanceRepository;
import com.yesh.apartmentledger.finance.maintenance.repository.MaintenancePaymentRepository;
import com.yesh.apartmentledger.master.approvalstatus.entity.ApprovalStatus;
import com.yesh.apartmentledger.master.approvalstatus.repository.ApprovalStatusRepository;
import com.yesh.apartmentledger.master.ledgercategory.entity.LedgerCategory;
import com.yesh.apartmentledger.master.ledgercategory.repository.LedgerCategoryRepository;
import com.yesh.apartmentledger.master.paymentmode.entity.PaymentMode;
import com.yesh.apartmentledger.master.paymentmode.repository.PaymentModeRepository;
import com.yesh.apartmentledger.master.referencetype.repository.ReferenceTypeRepository;
import com.yesh.apartmentledger.security.model.CustomUserDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service

public class IncomeDraftService {

    private final IncomeDraftRepository incomeDraftRepository;
    private final IncomeRepository incomeRepository;
    private final ApartmentRepository apartmentRepository;
    private final FlatRepository flatRepository;

    private final PaymentModeRepository paymentModeRepository;
    private final LedgerCategoryRepository ledgerCategoryRepository;
    private final ApprovalStatusRepository approvalStatusRepository;
    private final AppUserRepository appUserRepository;
    private final MaintenancePaymentRepository paymentRepository;
    private final MaintenanceAdvanceRepository advanceRepository; //
    private final LedgerValidationUtil legerValidationUtil;
    // Use Constructor Injection (Best Practice for Spring)
    public IncomeDraftService(IncomeDraftRepository incomeDraftRepository,
                              IncomeRepository incomeRepository,
                              ApartmentRepository apartmentRepository,
                              FlatRepository flatRepository,
                              LedgerCategoryRepository ledgerCategoryRepository,
                              PaymentModeRepository paymentModeRepository,
                            //  ReferenceTypeRepository referenceTypeRepository,
                              ApprovalStatusRepository approvalStatusRepository,
                              AppUserRepository appUserRepository, MaintenancePaymentRepository paymentRepository, MaintenanceAdvanceRepository advanceRepository, LedgerValidationUtil legerValidationUtil) {
        this.incomeDraftRepository = incomeDraftRepository;
        this.incomeRepository = incomeRepository;
        this.apartmentRepository = apartmentRepository;
        this.flatRepository = flatRepository;
        this.ledgerCategoryRepository = ledgerCategoryRepository;
        this.paymentModeRepository = paymentModeRepository;

        this.approvalStatusRepository = approvalStatusRepository;
        this.appUserRepository = appUserRepository;
        this.paymentRepository = paymentRepository;
        this.advanceRepository = advanceRepository;
        this.legerValidationUtil = legerValidationUtil;
    }
    // new
    @Transactional
    public void createDraft(Long apartmentId, IncomeDraftRequest request) {

        // 1. Fetch Multi-tenant context
        Apartment apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Apartment not found"));
         Long userid = 5L;
        // 2. Fetch the Maker
        AppUser maker = appUserRepository.findById(userid)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 3. Fetch Master Data
        Long categoryid = 1L;
        LedgerCategory category = ledgerCategoryRepository.findById(categoryid) //request.ledgerCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        PaymentMode paymentMode = paymentModeRepository.findById(request.paymentModeId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment Mode not found"));

        // 4. Fetch the initial 'PENDING' approval status.
        // Assuming 'PENDING' is a valid status_code in your master table.
        ApprovalStatus pendingStatus = approvalStatusRepository.findByStatusNameIgnoreCase(ApprovalStatusEnum.PENDING.name())
                .orElseThrow(() -> new ResourceNotFoundException("Default approval status not found"));

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


    /**
     * WORKFLOW STEP 1: Admin Creates a Draft
     */
    @Transactional
    public IncomeDraftResponse createIncomeDraft(IncomeDraftRequest request) {
        Long currentApartmentId = getCurrentApartmentId();

        IncomeDraft draft = new IncomeDraft();

        // 1. Map Core Identifiers
        draft.setApartment(apartmentRepository.getReferenceById(currentApartmentId));

        if (request.flatId() != null) {
            draft.setFlat(flatRepository.findById(request.flatId())
                    .orElseThrow(() -> new IllegalArgumentException("Flat not found")));
        }


        draft.setTransactionDate(request.transactionDate());
        draft.setAmount(request.amount());
        draft.setReferenceNumber(request.referenceNumber());
        draft.setRemarks(request.remarks());

        // 3. Set Status and Auditing
        ApprovalStatus pendingStatus = approvalStatusRepository.findByStatusNameIgnoreCase(ApprovalStatusEnum.PENDING.name())
                .orElseThrow(() -> new ResourceNotFoundException("PENDING status not defined in master data"));
        draft.setApprovalStatus(pendingStatus);

        // Use the context for created_by
        draft.setCreatedBy(getCurrentUser());
        // Note: BaseAuditEntity (via @EnableJpaAuditing) handles createdDate/updatedDate

        IncomeDraft savedDraft = incomeDraftRepository.save(draft);
        return mapToResponse(savedDraft);
    }

    /**
     * WORKFLOW STEP 2: Admin Approves the Draft
     * This pushes the data into the immutable `finance.income` ledger.
     */
    @Transactional
    public void approveDraft(Long draftId, IncomeApprovalRequest request) {
        Long currentApartmentId = getCurrentApartmentId();

        // 1. Fetch Draft (Ensure it belongs to this tenant)
        IncomeDraft draft = incomeDraftRepository.findByIdAndApartmentId(draftId, currentApartmentId)
                .orElseThrow(() -> new IllegalArgumentException("Draft not found"));

        if (!draft.getApprovalStatus().getStatusCode().equals("PENDING")) {
            throw new IllegalStateException("Only PENDING drafts can be approved.");
        }

        AppUser approver = getCurrentUser();

        // 2. Build the Final Immutable Ledger Entry
        Income finalLedger = new Income();
        finalLedger.setApartment(draft.getApartment());
        finalLedger.setFlat(draft.getFlat());

        finalLedger.setTransactionDate(draft.getTransactionDate());
        finalLedger.setAmount(draft.getAmount());
        finalLedger.setPaymentMode(draft.getPaymentMode());

        finalLedger.setReferenceNo(draft.getReferenceNumber());
        finalLedger.setRemarks(draft.getRemarks());

        // Preserve original author, but record approver
        finalLedger.setCreatedBy(draft.getCreatedBy());
        finalLedger.setApprovedBy(approver);
        finalLedger.setApprovedDate(LocalDateTime.now());

        // 3. Save Final Ledger
        Income savedLedger = incomeRepository.save(finalLedger);

        // 4. Update the Draft Record
        ApprovalStatus approvedStatus = approvalStatusRepository.findByStatusNameIgnoreCase("APPROVED")
                .orElseThrow(() -> new IllegalStateException("APPROVED status missing"));

        draft.setApprovalStatus(approvedStatus);
        // Assuming you add these fields to IncomeDraft entity to match your SQL:
        // draft.setApprovalComments(request.comments());
        // draft.setApprovedBy(approver);
        // draft.setApprovedDate(LocalDateTime.now());
        // draft.setIncome(savedLedger); 

        incomeDraftRepository.save(draft);
    }

    /**
     * WORKFLOW STEP 3: Admin Rejects the Draft
     */
    @Transactional
    public void rejectDraft(Long draftId, IncomeApprovalRequest request) {
        Long currentApartmentId = getCurrentApartmentId();

        IncomeDraft draft = incomeDraftRepository.findByIdAndApartmentId(draftId, currentApartmentId)
                .orElseThrow(() -> new IllegalArgumentException("Draft not found"));

        ApprovalStatus rejectedStatus = approvalStatusRepository.findByStatusNameIgnoreCase("REJECTED")
                .orElseThrow(() -> new IllegalStateException("REJECTED status missing"));

        draft.setApprovalStatus(rejectedStatus);
        // draft.setApprovalComments(request.comments());

        incomeDraftRepository.save(draft);
    }

    /**
     * Retrieve all Pending Drafts for the Approval Dashboard
     */
    public List<IncomeDraftResponse> getPendingDrafts() {
        Long currentApartmentId = getCurrentApartmentId();

        ApprovalStatus pendingStatus = approvalStatusRepository.findByStatusNameIgnoreCase(ApprovalStatusEnum.PENDING.name())
                .orElseThrow();

        return incomeDraftRepository.findByApartmentIdAndApprovalStatusId(currentApartmentId, pendingStatus.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    /*================================================
    // saveIncomeDrafts
    ===================================================== */
    @Transactional
    public String saveIncomeDrafts(Long apartmentId, List<IncomeDraftRequest> requests) {

        Apartment apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new RuntimeException("Apartment not found"));

        // For now, hardcode the Maker (User ID 1) and PENDING status (Status ID 1)
        // Once Auth is ready, you will pull the User ID from the SecurityContext
        AppUser maker = appUserRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Maker user not found"));

        ApprovalStatus pendingStatus = approvalStatusRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Approval status 'PENDING' not found"));

        List<IncomeDraft> draftsToSave = new ArrayList<>();

        for (IncomeDraftRequest request : requests) {
            // 1. Validate Go-Live Date (using the helper we built earlier)
            legerValidationUtil.validateBillingPeriod(apartment, request.txnYear(), request.txnMonth());

            Flat flat = flatRepository.findById(request.flatId())
                    .orElseThrow(() -> new RuntimeException("Flat not found: " + request.flatId()));

            PaymentMode paymentMode = paymentModeRepository.findById(request.paymentModeId())
                    .orElseThrow(() -> new RuntimeException("Payment mode not found"));

            // 2. Build the Draft Entity
            IncomeDraft draft = new IncomeDraft();
            draft.setApartment(apartment);
            draft.setFlat(flat);
            draft.setTransactionDate(request.transactionDate());
            draft.setTxnYear(request.txnYear());
            draft.setTxnMonth(request.txnMonth());
            draft.setAmount(request.amount());
            draft.setPaymentMode(paymentMode);
            draft.setReferenceNumber(request.referenceNumber());
            draft.setRemarks(request.remarks());

            // Audit & Status fields
            draft.setApprovalStatus(pendingStatus);
            draft.setCreatedBy(maker);

            draftsToSave.add(draft);
        }

        // 3. Batch save all drafts efficiently
        incomeDraftRepository.saveAll(draftsToSave);

        return draftsToSave.size() + " draft(s) successfully saved and sent for approval.";
    }

    // ==========================================
    // FETCH PENDING DRAFTS (Tab 2 - Approver Screen)
    // ==========================================

    @Transactional(readOnly = true)
    public List<IncomeDraftResponse> getPendingDrafts(Long apartmentId) {
        Long pendingStatusId = 1L; // Configured ID for 'PENDING'

        List<IncomeDraft> pendingDrafts = incomeDraftRepository
                .findByApartmentIdAndApprovalStatusId(apartmentId, pendingStatusId);

        return pendingDrafts.stream().map(this::mapToResponse).toList();

    }
    // ==========================================
    // APPROVE DRAFTS (The Master Commit)
    // ==========================================

    @Transactional
    public String approveDrafts(Long apartmentId, DraftApprovalRequest request) {

        // Hardcode Approver (User ID 2) and APPROVED status (Status ID 2) for now
        AppUser approver = appUserRepository.findById(2L)
                .orElseThrow(() -> new ResourceNotFoundException("Approver user not found"));

        ApprovalStatus approvedStatus = approvalStatusRepository.findById(2L)
                .orElseThrow(() -> new ResourceNotFoundException("Approval status 'APPROVED' not found"));

        List<MaintenancePayment> finalLedgerEntries = new ArrayList<>();
        List<IncomeDraft> draftsToUpdate = new ArrayList<>(); // <-- FIX: Initialize the list!

        for (Long draftId : request.draftIds()) {
            IncomeDraft draft = incomeDraftRepository.findById(draftId)
                    .orElseThrow(() -> new ResourceNotFoundException("Draft not found: " + draftId));

            // 1. Update the Draft Audit Trail
            draft.setApprovalStatus(approvedStatus);
            draftsToUpdate.add(draft); // <-- FIX: Add the updated draft to our list

            // 2. Create the Immutable Ledger Entry
            MaintenancePayment payment = new MaintenancePayment();
            payment.setApartment(draft.getApartment());
            payment.setFlat(draft.getFlat());
            payment.setTransactionDate(draft.getTransactionDate());
            payment.setTxnYear(draft.getTxnYear());
            payment.setTxnMonth(draft.getTxnMonth());
            payment.setAmount(draft.getAmount());
            payment.setPaymentMode(draft.getPaymentMode());
            payment.setReferenceNumber(draft.getReferenceNumber());
            payment.setRemarks(draft.getRemarks());

            // Maker-Checker Audit Link
            payment.setCreatedBy(draft.getCreatedBy());
            payment.setApprovedBy(approver);

            finalLedgerEntries.add(payment);

            // 3. Wallet Deduction Logic (If they paid via Advance)
            // Assuming PaymentMode ID '3' is 'Advance Wallet'
            if (draft.getPaymentMode().getId().equals(3L)) {
                deductFromAdvanceWallet(draft.getFlat().getId(), draft.getAmount());
            }
        }

        // Batch save the final entries and updated drafts
        paymentRepository.saveAll(finalLedgerEntries);
        incomeDraftRepository.saveAll(draftsToUpdate); // <-- FIX: Save the corrected list!

        return request.draftIds().size() + " draft(s) successfully approved and posted to the ledger.";
    }

    // --- Helper method for FIFO Wallet Deduction ---
    private void deductFromAdvanceWallet(Long flatId, BigDecimal amountToTake) {
        BigDecimal amountLeftToDeduct = amountToTake;

        List<MaintenanceAdvance> activeAdvances = advanceRepository
                .findByFlatIdAndRemainingBalanceGreaterThanOrderByReceiptDateAsc(flatId, BigDecimal.ZERO);

        for (MaintenanceAdvance advance : activeAdvances) {
            if (amountLeftToDeduct.compareTo(BigDecimal.ZERO) <= 0) break;

            BigDecimal deduction = advance.getRemainingBalance().min(amountLeftToDeduct);
            advance.setRemainingBalance(advance.getRemainingBalance().subtract(deduction));
            advanceRepository.save(advance);

            amountLeftToDeduct = amountLeftToDeduct.subtract(deduction);
        }

        if (amountLeftToDeduct.compareTo(BigDecimal.ZERO) > 0) {
            throw new BadRequestException("Not enough balance in the advance wallet to cover this deduction!");
        }
    }

    @Transactional
    public void updateDraft(Long apartmentId, Long draftId, IncomeDraftRequest payload) {
        // find the draftId
        // 1. Fetch the draft and ensure it belongs to the specified apartment
        var income = incomeDraftRepository.findByIdAndApartmentId(draftId, apartmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Income draft id [" + draftId + "] does not exist."));

        // 2. Business Rule: Prevent editing of locked/approved drafts
        // (Adjust "APPROVED" to match your actual Enum or String status variable)
        if (income.getApprovalStatus().toString().equals(ApprovalStatusEnum.APPROVED.name())) {
            throw new IllegalStateException("Cannot update an income draft that has already been approved.");
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

    /**
     * Helper to safely extract the Apartment ID from the JWT Context
     */
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