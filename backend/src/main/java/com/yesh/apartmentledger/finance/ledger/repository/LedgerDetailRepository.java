package com.yesh.apartmentledger.finance.ledger.repository;

import com.yesh.apartmentledger.finance.ledger.entity.LedgerDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LedgerDetailRepository extends JpaRepository<LedgerDetail,String> {

    @Query("SELECT l FROM LedgerDetail l WHERE l.apartmentId = :apartmentId " +
            "AND (:year IS NULL OR l.txnYear = :year) " +
            "AND (:month IS NULL OR l.txnMonth = :month) " +
            "ORDER BY l.transactionDate ASC")
    List<LedgerDetail> findByApartmentAndOptionalPeriod(
            @Param("apartmentId") Long apartmentId,
            @Param("year") Short year,
            @Param("month") Short month
    );
//    // Get summary
//    @Query("""
//    SELECT new com.yesh.apartmentledger.finance.ledger.dto.MonthlySummaryResponse(
//        COALESCE(SUM(l.incomeAmount), 0),
//        COALESCE(SUM(l.expenseAmount), 0))
//    FROM LedgerDetail l
//    WHERE l.apartmentId = :apartmentId
//      AND (:year IS NULL OR l.txnYear = :year)
//      AND (:month IS NULL OR l.txnMonth = :month)
//    """)
//    MonthlySummaryResponse getTotalIncomeAndExpense(
//            @Param("apartmentId") Long apartmentId,
//            @Param("year") Short year,
//            @Param("month") Short month);
}
