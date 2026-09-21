package com.yesh.apartmentledger.finance.expense.repository;

import com.yesh.apartmentledger.finance.expense.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

   @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.apartment.id = :apartmentId AND e.txnYear = :year AND e.txnMonth = :month")
   BigDecimal getTotalExpenseForMonth(Long apartmentId, Short year, Short month);
}