package com.yesh.apartmentledger.master.role.repository;

import com.yesh.apartmentledger.master.role.entity.Role;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for User Roles
 */
@Repository
public interface RoleRepository extends JpaRepository<Role,Long> {

     Optional<Role> findByRoleNameIgnoreCase(String roleName);
     Optional<Role> findById(@NotNull Long roleId);
}
