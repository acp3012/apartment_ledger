package com.yesh.apartmentledger.master.referencetype.entity;

import com.yesh.apartmentledger.common.entity.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "reference_type", schema = "master")
@Getter
@Setter
@NoArgsConstructor
public class ReferenceType extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reference_type_id")
    private Long id;

    @Column(name = "reference_type_name", nullable = false, unique = true, length = 40)
    private String referenceTypeName;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 1;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}