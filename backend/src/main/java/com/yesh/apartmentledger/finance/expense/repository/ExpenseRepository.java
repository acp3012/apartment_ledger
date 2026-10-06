package com.yesh.apartmentledger.finance.expense.repository;

import com.yesh.apartmentledger.finance.expense.dto.LedgerCategoryExpenseSummary;
import com.yesh.apartmentledger.finance.expense.entity.Expense;
import com.yesh.apartmentledger.finance.income.entity.Income;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

   @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.apartment.id = :apartmentId AND e.txnYear = :year AND e.txnMonth = :month")
   BigDecimal getTotalExpenseForMonth(Long apartmentId, Short year, Short month);


//   @Query("SELECT e FROM Expense e " +
//           "JOIN FETCH e.apartment a " +
//           "JOIN FETCH e.ledgerCategory lc " +
//           "JOIN FETCH e.paymentMode pm " +
//           "JOIN FETCH e.createdBy cb " +
//           "JOIN FETCH e.approvedBy ab " +
//           "WHERE a.id = :apartmentId " +
//           "AND (:year IS NULL OR e.txnYear = :year) " +
//           "AND (:month IS NULL OR e.tnxMonth = :month)")
//   List<Expense> findByApartmentYearMonth(
//           @Param("apartmentId") Long apartmentId,
//           @Param("year") Short year,
//           @Param("month") Short month
//   );

//   @Query("SELECT e FROM Expense e " +
//           "JOIN FETCH e.apartment a " +
//           "JOIN FETCH e.ledgerCategory lc " +
//           "JOIN FETCH e.paymentMode pm " +
//           "JOIN FETCH e.createdBy cb " +
//           "JOIN FETCH e.approvedBy ab " +
//           "WHERE a.id = :apartmentId " +
//           "AND (:year IS NULL OR e.txnYear = :year) " +
//           "AND (:month IS NULL OR e.txnMonth = :month)")
//   List<Expense> findExpensesByApartmentAndPeriod(
//           @Param("apartmentId") Long apartmentId,
//           @Param("year") Short year,
//           @Param("month") Short month
//   );

   @Query("SELECT e FROM Expense e " +
           "JOIN FETCH e.apartment a " +
           "JOIN FETCH e.ledgerCategory lc " +
           "JOIN FETCH e.paymentMode pm " +
           "JOIN FETCH e.createdBy cb " +
           "JOIN FETCH e.approvedBy ab " +
           "WHERE a.id = :apartmentId " +
           "AND (:year IS NULL OR e.txnYear = :year) " +
           "AND (:month IS NULL OR e.txnMonth = :month)")
   List<Expense> findExpensesByApartmentAndPeriod(
           @Param("apartmentId") Long apartmentId,
           @Param("year") Short year,
           @Param("month") Short month
   );

   @Query("""
           SELECT  new com.yesh.apartmentledger.finance.expense.dto.LedgerCategoryExpenseSummary(
           lc.categoryName, SUM(e.amount), count(e),
                      e.txnYear, e.txnMonth, e.apartment.id         )
           FROM  Expense e
           JOIN  e.ledgerCategory lc
           WHERE e.apartment.id = :apartmentId
           AND (:year is null OR e.txnYear =:year)
           AND (:month is null OR e.txnMonth =:month)
           GROUP BY lc.categoryName,e.txnYear, e.txnMonth, e.apartment.id
           """)
   public List<LedgerCategoryExpenseSummary> getExpenseSummaryByCategory(
           @Param("apartmentId") Long apartmentId,
           @Param("year") Short year,
           @Param("month") Short month
   );
}