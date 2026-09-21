package com.yesh.apartmentledger.finance.ledger.entity;

import com.yesh.apartmentledger.common.entity.BaseAuditEntity;
import com.yesh.apartmentledger.core.apartment.entity.Apartment;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(schema = "finance", name="ledger_period")
public class LedgerPeriod  extends BaseAuditEntity {
    @Id
   private Long apartmentId;

    @Column(name = "active_month", nullable = false)
    private Short activeMonth;

    @Column(name = "active_year", nullable = false)
    private Short activeYear;

    @Column(name = "month_name", nullable = false, updatable = false)
    private String monthName;


}
