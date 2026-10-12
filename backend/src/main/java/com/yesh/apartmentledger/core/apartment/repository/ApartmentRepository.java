package com.yesh.apartmentledger.core.apartment.repository;

 import com.yesh.apartmentledger.core.apartment.entity.Apartment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ApartmentRepository extends JpaRepository<Apartment, Long> {

    List<Apartment> findAll();
    Optional<Apartment> findById(Long   id);
    boolean existsByApartmentCode(String apartmentCode);
}