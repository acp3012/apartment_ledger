package com.yesh.apartmentledger.finance.ledger.repository;

import com.yesh.apartmentledger.finance.ledger.entity.MonthlyLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MonthlyLedgerRepository extends JpaRepository<MonthlyLedger, Integer> {

    // 1. Fetch a specific ledger (useful for historical reporting)
    Optional<MonthlyLedger> findByApartmentIdAndYearAndMonth(Long apartmentId, Short year, Short month);

    // 2.  Find the currently OPEN month (Required for your IncomeDraftService validation)
    Optional<MonthlyLedger> findByApartmentIdAndStatus(Long apartmentId, String status);

    // 3. Check if a specific month is explicitly CLOSED
    boolean existsByApartmentIdAndYearAndMonthAndStatus(Long apartmentId, Short year, Short month, String status);

}