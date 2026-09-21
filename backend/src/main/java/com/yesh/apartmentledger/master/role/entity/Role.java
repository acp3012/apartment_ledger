package com.yesh.apartmentledger.master.role.entity;

import com.yesh.apartmentledger.common.entity.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "role", schema = "master")
@Getter
@Setter
@NoArgsConstructor
public class Role extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Long id;

    @Column(name = "role_name", nullable = false, unique = true, length = 30)
    private String roleName;

    @Column(name = "role_description", length = 100)
    private String roleDescription;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
