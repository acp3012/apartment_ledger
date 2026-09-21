package com.yesh.apartmentledger.core.apartment.repository;

 import com.yesh.apartmentledger.core.apartment.entity.Apartment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApartmentRepository extends JpaRepository<Apartment, Long> {

    List<Apartment> findAll();
    Optional<Apartment> findById(Long id);
}