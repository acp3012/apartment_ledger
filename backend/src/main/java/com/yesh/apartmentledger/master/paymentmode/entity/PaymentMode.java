package com.yesh.apartmentledger.master.paymentmode.entity;

import com.yesh.apartmentledger.common.entity.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "payment_mode", schema = "master")
@Getter
@Setter
@NoArgsConstructor
public class PaymentMode extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_mode_id")
    private Long id;

    @Column(name = "payment_mode_name", nullable = false, unique = true, length = 30)
    private String paymentModeName;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 1;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}