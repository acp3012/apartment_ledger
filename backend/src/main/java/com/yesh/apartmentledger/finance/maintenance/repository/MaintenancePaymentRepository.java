package com.yesh.apartmentledger.finance.maintenance.repository;

import com.yesh.apartmentledger.finance.maintenance.entity.MaintenancePayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface MaintenancePaymentRepository extends JpaRepository<MaintenancePayment, Long> {

    // 1. Fetch ledger for a specific apartment (Security isolation)
    List<MaintenancePayment> findByApartmentIdOrderByTxnYearDescTxnMonthDesc(Long apartmentId);

    // 2. Prevent Double Billing!
    // Checks if we already deducted money for this flat for a specific month/year.
    boolean existsByFlatIdAndTxnYearAndTxnMonth(Long flatId, Integer txnYear, Integer txnMonth);

    @Query("SELECT COALESCE(SUM(mp.amount), 0) FROM MaintenancePayment mp " +
            "WHERE mp.flat.id = :flatId AND mp.txnYear = :txnYear AND mp.txnMonth = :txnMonth")
    BigDecimal getTotalPaidForFlatAndMonth(
            @Param("flatId") Long flatId,
            @Param("txnYear") Integer txnYear,
            @Param("txnMonth") Integer txnMonth);

    @Query("SELECT SUM(m.amount) FROM MaintenancePayment m WHERE m.apartment.id = :apartmentId AND m.txnYear = :year AND m.txnMonth = :month")
    BigDecimal getTotalIncomeForMonth(Long apartmentId, Short year, Short month);
}