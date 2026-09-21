package com.yesh.apartmentledger.finance.maintenance.entity;

import com.yesh.apartmentledger.core.apartment.entity.Apartment;
import com.yesh.apartmentledger.core.flat.entity.Flat;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "maintenance_due", schema = "finance")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class MaintenanceDue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "maintenance_due_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartment_id", nullable = false)
    private Apartment apartment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flat_id", nullable = false)
    private Flat flat;

    @Column(name = "txn_year", nullable = false)
    private Integer txnYear;

    @Column(name = "txn_month", nullable = false)
    private Integer txnMonth;

    @Column(name = "due_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal dueAmount;

    @Column(name = "paid_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    // Computed column in DB; JPA should only read this value.
    @Column(name = "balance_amount", precision = 12, scale = 2, insertable = false, updatable = false)
    private BigDecimal balanceAmount;

    @Column(name = "due_status", nullable = false, length = 20)
    private String dueStatus; // PENDING, PARTIAL, PAID

    @CreatedDate
    @Column(name = "generated_date", nullable = false, updatable = false)
    private LocalDateTime generatedDate;
}