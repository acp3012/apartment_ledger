package com.yesh.apartmentledger.master.ledgercategory.repository;

import com.yesh.apartmentledger.master.ledgercategory.entity.LedgerCategory;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LedgerCategoryRepository extends JpaRepository<LedgerCategory, Long> {

    Optional<LedgerCategory> findById(Long id);
    List<LedgerCategory> findByTransactionTypeIgnoreCase(String transactionType);
    List<LedgerCategory> findAll();
    List<LedgerCategory> findByTransactionTypeAndIsActiveTrueOrderByDisplayOrderAsc(String transactionType);

}