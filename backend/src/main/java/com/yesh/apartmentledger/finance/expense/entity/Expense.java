package com.yesh.apartmentledger.finance.expense.entity;

import com.yesh.apartmentledger.core.apartment.entity.Apartment;

import com.yesh.apartmentledger.core.user.entity.AppUser;
import com.yesh.apartmentledger.master.approvalstatus.entity.ApprovalStatus;
import com.yesh.apartmentledger.master.ledgercategory.entity.LedgerCategory;

import com.yesh.apartmentledger.master.paymentmode.entity.PaymentMode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(schema = "finance", name = "expense")
@Getter
@Setter
public class Expense  {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "expense_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartment_id", nullable = false)
    private Apartment apartment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ledger_category_id", nullable = false)
    private LedgerCategory ledgerCategory;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "txn_year", nullable = false, updatable = false)
    private Short txnYear;

    @Column(name = "txn_month", nullable = false, updatable = false)
    private Short txnMonth;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "reference_number", nullable = true, length = 50)
    private String referenceNumber;

    @Column(name = "remarks", length = 500)
    private String remarks;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_mode_id", nullable = false)
    private PaymentMode paymentMode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private AppUser createdBy;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by", nullable = false)
    private AppUser approvedBy;

    @Column(name = "approved_date", nullable = false, updatable = false)
    private LocalDateTime approvedDate;
}
