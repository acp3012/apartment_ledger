package com.yesh.apartmentledger.core.user.repository;


import com.yesh.apartmentledger.core.user.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    boolean existsByEmail(String email);
    Optional<AppUser> findByEmail(String email);
    boolean existsByEmailAndApartmentId(String email, Long apartmentId);

    Optional<AppUser> findByEmailAndIsActiveTrue(String email);
    
}