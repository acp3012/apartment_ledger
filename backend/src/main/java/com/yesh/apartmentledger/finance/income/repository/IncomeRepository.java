package com.yesh.apartmentledger.finance.income.repository;

import com.yesh.apartmentledger.finance.income.entity.Income;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncomeRepository extends JpaRepository<Income, Long> {

    // Fetch the final ledger securely for the current tenant
    List<Income> findByApartmentId(Long apartmentId);

    // Used for reporting (e.g., getting all income for a specific flat)
    List<Income> findByApartmentIdAndFlatId(Long apartmentId, Long flatId);
}