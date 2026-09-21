package com.yesh.apartmentledger.finance.income.repository;

import com.yesh.apartmentledger.finance.income.entity.IncomeDraft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IncomeDraftRepository extends JpaRepository<IncomeDraft, Long> {

    // Fetch a draft securely by ensuring it belongs to the current tenant
    Optional<IncomeDraft> findByIdAndApartmentId(Long id, Long apartmentId);
    // Fetch all pending drafts (Status ID 1) for the apartment
    List<IncomeDraft> findByApartmentIdAndApprovalStatusId(Long apartmentId, Long statusId);
    // Notice how we traverse the relationship: ApprovalStatus_StatusName
    //List<IncomeDraft> findByApartmentIdAndApprovalStatus_StatusName(Long apartmentId, String statusName);
}
