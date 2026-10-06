package com.yesh.apartmentledger.master.approvalstatus.repository;

import com.yesh.apartmentledger.master.approvalstatus.entity.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApprovalStatusRepository extends JpaRepository<ApprovalStatus, Long> {

    List<ApprovalStatus> findAll();

    Optional<ApprovalStatus> findByStatusNameIgnoreCase(String statusName);
}