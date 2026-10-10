package com.yesh.apartmentledger.finance.ledger.entity;

import com.yesh.apartmentledger.common.entity.BaseAuditEntity;
import com.yesh.apartmentledger.core.apartment.entity.Apartment;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(schema = "finance", name = "monthly_ledger")
@Getter
@Setter
public class MonthlyLedger extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "monthly_ledger_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartment_id", nullable = false)
    private Apartment apartment;

    @Column(name = "month", nullable = false)
    private Short month;

    @Column(name = "year", nullable = false)
    private Short year;

    @Column(name = "opening_balance", precision = 16, scale = 2)
    private BigDecimal openingBalance;

    @Column(name = "income", precision = 12, scale = 2)
    private BigDecimal income;

    @Column(name = "expense", precision = 12, scale = 2)
    private BigDecimal expense;

    // Database generates this, so Hibernate should only read it!
    @Column(name = "closing_balance", precision = 16, scale = 2, insertable = false, updatable = false)
    private BigDecimal closingBalance;

}