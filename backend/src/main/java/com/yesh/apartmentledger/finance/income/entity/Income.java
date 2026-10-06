package com.yesh.apartmentledger.finance.income.entity;
import com.yesh.apartmentledger.core.apartment.entity.Apartment;
import com.yesh.apartmentledger.core.flat.entity.Flat;
import com.yesh.apartmentledger.core.user.entity.AppUser;
import com.yesh.apartmentledger.master.ledgercategory.entity.LedgerCategory;
import com.yesh.apartmentledger.master.paymentmode.entity.PaymentMode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "income", schema = "finance")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class Income {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "income_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartment_id", nullable = false, updatable = false)
    private Apartment apartment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flat_id", updatable = false)
    private Flat flat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ledger_category_id", nullable = false, updatable = false)
    private LedgerCategory ledgerCategory;

    @Column(name = "transaction_date", nullable = false, updatable = false)
    private LocalDate transactionDate;

    @Column(name = "txn_year", updatable = false, insertable = false)
    private Short year ;

    @Column(name = "txn_month", updatable = false, insertable = false)
    private Short month;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2, updatable = false)
    private BigDecimal amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_mode_id", nullable = false, updatable = false)
    private PaymentMode paymentMode;


    @Column(name = "reference_number", length = 50, updatable = false)
    private String referenceNo;

    @Column(name = "remarks", length = 500, updatable = false)
    private String remarks;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false, updatable = false)
    private AppUser createdBy;

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by", nullable = false, updatable = false)
    private AppUser approvedBy;

    @Column(name = "approved_date", nullable = false, updatable = false)
    private LocalDateTime approvedDate;
}
