package com.yesh.apartmentledger.finance.income.repository;

import com.yesh.apartmentledger.finance.income.entity.IncomeDraft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface IncomeDraftRepository extends JpaRepository<IncomeDraft, Long> {

    boolean existsByApartment_Id(Long apartmentId);

    // Fetch a draft securely by ensuring it belongs to the current tenant
    Optional<IncomeDraft> findByIdAndApartmentId(Long id, Long apartmentId);

    List<IncomeDraft> findByApartment_IdAndApprovalStatus_StatusNameAndTxnYearAndTxnMonth(
            Long apartmentId,
            String statusName, // Now you can pass "PENDING" here safely!
            Short txnYear,
            Short txnMonth
        );
    // use for duplicate entry check
    List<IncomeDraft> findByApartment_IdAndFlat_IdAndTransactionDateAndLedgerCategory_Id(
            Long apartmentId, Long flatId, LocalDate transactionDate, Long ledgerCategoryId);

    boolean existsByApartment_IdAndFlat_IdAndTransactionDateAndLedgerCategory_Id(
            Long apartmentId, Long flatId, LocalDate transactionDate, Long ledgerCategoryId);;

    @Query("SELECT i FROM IncomeDraft i WHERE i.apartment.id = :apartmentId " +
            "AND (cast(:statusId as long) IS NULL OR i.approvalStatus.id = :statusId) " +
            "AND (cast(:year as short) IS NULL OR i.txnYear = :year) " +
            "AND (cast(:month as short) IS NULL OR i.txnMonth = :month)")
    List<IncomeDraft> findDynamicDrafts(
            @Param("apartmentId") Long apartmentId,
            @Param("statusId") Long statusId,
            @Param("year") Short year,
            @Param("month") Short month
    );

}
