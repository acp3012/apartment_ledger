package com.yesh.apartmentledger.finance.maintenance.entity;

import com.yesh.apartmentledger.common.entity.BaseAuditEntity;
import com.yesh.apartmentledger.core.flat.entity.Flat;
import com.yesh.apartmentledger.master.paymentmode.entity.PaymentMode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "maintenance_advance", schema = "finance")
@Getter
@Setter
@NoArgsConstructor
public class MaintenanceAdvance extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flat_id", nullable = false)
    private Flat flat;

    @Column(name = "receipt_date", nullable = false)
    private LocalDate receiptDate;

    @Column(name = "advance_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal advanceAmount;

    @Column(name = "remaining_balance", nullable = false, precision = 10, scale = 2)
    private BigDecimal remainingBalance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_mode_id", nullable = false)
    private PaymentMode paymentMode;

    @Column(name = "reference_number", length = 100)
    private String referenceNumber;

    /**
     * While BaseAuditEntity handles timestamps, we still use @PrePersist
     * here to automatically initialize the wallet balance.
     */
    @PrePersist
    protected void initializeBalance() {
        if (remainingBalance == null) {
            remainingBalance = advanceAmount;
        }
    }
}