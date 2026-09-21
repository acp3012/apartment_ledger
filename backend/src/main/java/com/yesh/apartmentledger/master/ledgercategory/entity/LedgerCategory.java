package com.yesh.apartmentledger.master.ledgercategory.entity;

import com.yesh.apartmentledger.common.entity.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ledger_category", schema = "master")
@Getter
@Setter
@NoArgsConstructor
public class LedgerCategory extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ledger_category_id")
    private Long id;

    @Column(name = "category_name", nullable = false, unique = true, length = 100)
    private String categoryName;

    @Column(name = "transaction_type", length = 2)
    private String transactionType; // 'CR' or 'DR'

    @Column(name = "is_flat_maintenance", nullable = false)
    private Boolean isFlatMaintenance = false;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 1;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}