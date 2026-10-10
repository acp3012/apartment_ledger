package com.yesh.apartmentledger.finance.ledger.repository;

import com.yesh.apartmentledger.finance.ledger.entity.MonthlyLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MonthlyLedgerRepository extends JpaRepository<MonthlyLedger, Long> {

    // 1. Fetch a specific ledger (useful for historical reporting)
    Optional<MonthlyLedger> findByApartmentIdAndYearAndMonth(Long apartmentId, Short year, Short month);


    //  Check if a specific month is explicitly CLOSED
    boolean existsByApartmentIdAndYearAndMonth(Long apartmentId, Short year, Short month);

}