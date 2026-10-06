package com.yesh.apartmentledger.finance.ledger.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Immutable       // view
@Getter
@Table(name="v_income_expense", schema = "finance")
public class LedgerDetail {
    @Id
    @Column(name="transaction_id", nullable = false)
    private String id;
    @Column(name="apartment_id")
    private Long apartmentId;
    @Column(name="apartment_name")
    private String apartmentName;
    @Column(name="transaction_date")
    private LocalDate transactionDate;
    @Column(name="txn_year")
    private Short txnYear;
    @Column(name="txn_month")
    private Short txnMonth;
    @Column(name="category_name")
    private String categoryName;
    @Column(name = "payment_mode_name")
    private String paymentModeName;
    @Column(name = "transaction_type")
    private String transactionType;
    @Column(name = "reference_number")
    private String referenceNumber;
    @Column(name = "remarks")
    private String remarks;
    @Column(name = "is_flat_maintenance")
    private Boolean isFlatMaintenance;

    @Column(name = "flat_number")
    private String flatNumber;
    @Column(name = "owner_name")
    private String ownerName;
    @Column(name = "income_amount")
    private BigDecimal incomeAmount;
    @Column(name = "expense_amount")
    private BigDecimal expenseAmount;

}
