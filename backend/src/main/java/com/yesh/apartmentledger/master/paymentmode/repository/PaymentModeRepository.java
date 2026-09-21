package com.yesh.apartmentledger.master.paymentmode.repository;

import com.yesh.apartmentledger.core.flat.entity.Flat;
import com.yesh.apartmentledger.finance.maintenance.dto.AdvancePaymentRequest;
import com.yesh.apartmentledger.master.paymentmode.entity.PaymentMode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentModeRepository extends JpaRepository<PaymentMode, Long> {
    @Override
    Optional<PaymentMode> findById(Long paymentModeId);
    Optional<PaymentMode> findByPaymentModeNameIgnoreCase(String modeName);
    // Fetches active payment modes and sorts them by your displayOrder column!
    List<PaymentMode> findByIsActiveTrueOrderByDisplayOrderAsc();

}