package com.yesh.apartmentledger.finance.ledger.repository;

import com.yesh.apartmentledger.finance.ledger.entity.LedgerPeriod;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LedgerPeriodRepository extends JpaRepository<LedgerPeriod,Long> {

    /**
     * @param apartmentId must not be {@literal null}.
     * @return LedgerPeriod
     */
     Optional<LedgerPeriod> findById(@NonNull Long apartmentId) ;

}
