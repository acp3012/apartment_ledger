package com.yesh.apartmentledger.core.flat.repository;

import com.yesh.apartmentledger.core.flat.entity.Flat;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FlatRepository extends JpaRepository<Flat, Long> {

    // Used during registration to validate the owner's mobile number
    Optional<Flat> findByOwnerMobile(String ownerMobile);

    Optional<Flat> findByOwnerMobileAndApartmentId(String ownerMobile, Long apartmentId);

    // Fetch all flats for a specific apartment
    List<Flat> findByApartmentId(Long apartmentId);

    Optional<Flat> findByIdAndApartmentId(Long flatId, Long apartmentId);

    Optional<Flat> findByFlatNumberAndApartmentId(String flatNumber, Long apartmentId);

    boolean existsByApartment_Id(Long apartmentId);

}