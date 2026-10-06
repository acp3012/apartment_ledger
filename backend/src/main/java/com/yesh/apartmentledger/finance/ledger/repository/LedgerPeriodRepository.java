package com.yesh.apartmentledger.finance.ledger.repository;

import com.yesh.apartmentledger.finance.ledger.entity.LedgerPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LedgerPeriodRepository extends JpaRepository<LedgerPeriod,Long> {
     Optional<LedgerPeriod> findById(Long id) ;
}
