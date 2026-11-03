package com.yesh.apartmentledger.finance.expense.repository;

import com.yesh.apartmentledger.finance.expense.entity.ExpenseDraft;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ExpenseDraftRepository extends JpaRepository<ExpenseDraft, Long> {

   boolean existsByApartment_Id(Long apartmentId);

   // Notice how we traverse the relationship: ApprovalStatus_StatusName
    List<ExpenseDraft> findByApartmentIdAndApprovalStatus_StatusName(Long apartmentId, String statusName);

    Optional<ExpenseDraft> findByIdAndApartmentId(Long id, Long apartmentId);
}