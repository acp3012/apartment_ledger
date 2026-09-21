package com.yesh.apartmentledger.finance.maintenance.repository;
import com.yesh.apartmentledger.finance.maintenance.entity.MaintenanceAdvance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface MaintenanceAdvanceRepository extends JpaRepository<MaintenanceAdvance, Long> {

    List<MaintenanceAdvance> findAllByOrderByReceiptDateDesc();

    // The FIFO query for auto-deduction
    List<MaintenanceAdvance> findByFlatIdAndRemainingBalanceGreaterThanOrderByReceiptDateAsc(Long flatId, BigDecimal balance);

    // COALESCE ensures it returns 0 instead of null if the flat has no advance records
    @Query("SELECT COALESCE(SUM(ma.remainingBalance), 0) FROM MaintenanceAdvance ma WHERE ma.flat.id = :flatId")
    BigDecimal getTotalAdvanceBalanceForFlat(@Param("flatId") Long flatId);

    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.apartment.id = :apartmentId AND e.txnYear = :year AND e.txnMonth = :month")
    BigDecimal getTotalExpenseForMonth(Long apartmentId, Short year, Short month);
}