package com.yesh.apartmentledger.finance.expense.repository;

import com.yesh.apartmentledger.finance.expense.entity.ExpenseDraft;
import com.yesh.apartmentledger.finance.income.entity.IncomeDraft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseDraftRepository extends JpaRepository<ExpenseDraft, Long> {

   // Notice how we traverse the relationship: ApprovalStatus_StatusName
    List<ExpenseDraft> findByApartmentIdAndApprovalStatus_StatusName(Long apartmentId, String statusName);

    Optional<ExpenseDraft> findByIdAndApartmentId(Long apartmentId, Long expenseDraftId);
}