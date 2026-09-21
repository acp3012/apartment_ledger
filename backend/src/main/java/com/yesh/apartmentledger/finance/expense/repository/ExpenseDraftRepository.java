package com.yesh.apartmentledger.finance.expense.repository;

import com.yesh.apartmentledger.finance.expense.entity.ExpenseDraft;
import com.yesh.apartmentledger.finance.income.entity.IncomeDraft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseDraftRepository extends JpaRepository<ExpenseDraft, Long> {

    // Fetch pending drafts (Status ID 1) for the checker screen
    List<ExpenseDraft> findByApartmentIdAndApprovalStatusId(Long apartmentId, Long statusId);
    // Notice how we traverse the relationship: ApprovalStatus_StatusName
    List<ExpenseDraft> findByApartmentIdAndApprovalStatus_StatusName(Long apartmentId, String statusName);
}