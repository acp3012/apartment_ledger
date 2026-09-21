package com.yesh.apartmentledger.master.approvalstatus.entity;

import com.yesh.apartmentledger.common.entity.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "approval_status", schema = "master")
@Getter
@Setter
@NoArgsConstructor
public class ApprovalStatus extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "approval_status_id")
    private Long id;

    @Column(name = "status_code", nullable = false, unique = true, length = 20)
    private String statusCode;

    @Column(name = "status_name", nullable = false, unique = true, length = 50)
    private String statusName;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 1;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}