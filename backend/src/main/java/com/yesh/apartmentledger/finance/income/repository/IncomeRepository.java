package com.yesh.apartmentledger.finance.income.repository;

import com.yesh.apartmentledger.finance.income.dto.PaymentModeIncomeSummary;
import com.yesh.apartmentledger.finance.income.entity.Income;
import com.yesh.apartmentledger.finance.income.entity.IncomeDraft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface IncomeRepository extends JpaRepository<Income, Long> {

    @Query("SELECT i FROM Income i " +
            "JOIN FETCH i.apartment a " +
            "LEFT JOIN FETCH i.flat f " +
            "JOIN FETCH i.ledgerCategory lc " +
            "JOIN FETCH i.paymentMode pm " +
            "JOIN FETCH i.createdBy cb " +
            "JOIN FETCH i.approvedBy ab " +
            "WHERE i.apartment.id = :apartmentId " +
            "AND (:year IS NULL OR i.year = :year) " +
            "AND (:month IS NULL OR i.month = :month)")
    List<Income> findByApartmentYearMonth(
            @Param("apartmentId") Long apartmentId,
            @Param("year") Short year,
            @Param("month") Short month
    );
    // get Total income
    @Query("SELECT COALESCE(SUM(i.amount),0) FROM Income i " +
            "WHERE i.apartment.id = :apartmentId " +
            "AND i.year = :year AND i.month = :month"
    )
    BigDecimal getTotalIncomeForMonth(Long apartmentId, Short year, Short month);

    @Query("""
    SELECT new com.yesh.apartmentledger.finance.income.dto.PaymentModeIncomeSummary(
        pm.paymentModeName,
        SUM(i.amount),
        COUNT(i),
        i.year,
        i.month,
        i.apartment.id)
    FROM Income i
    JOIN i.paymentMode pm
    WHERE i.apartment.id = :apartmentId
      AND (:txnYear IS NULL OR i.year = :txnYear)
      AND (:txnMonth IS NULL OR i.month = :txnMonth)
    GROUP BY pm.paymentModeName,i.year,
        i.month,
        i.apartment.id
    """)
    List<PaymentModeIncomeSummary> sumIncomeByPaymentMode(
            @Param("apartmentId") Long apartmentId,
            @Param("txnYear") Short year,
            @Param("txnMonth") Short month);
}