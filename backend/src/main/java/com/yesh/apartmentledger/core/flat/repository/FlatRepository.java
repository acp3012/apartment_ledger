package com.yesh.apartmentledger.core.flat.repository;


import com.yesh.apartmentledger.core.flat.entity.Flat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlatRepository extends JpaRepository<Flat, Long> {

    // Used during registration to validate the owner's mobile number
    Optional<Flat> findByOwnerMobile(String ownerMobile);

    // Fetch all flats for a specific apartment
    List<Flat> findByApartmentId(Long apartmentId);

    Optional<Flat> findByApartmentIdAndFlatNumber(Long apartmentId, String flatNumber);
}