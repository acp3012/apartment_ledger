package com.yesh.apartmentledger.finance.maintenance.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.yesh.apartmentledger.finance.maintenance.entity.MaintenanceRate;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface MaintenanceRateRepository extends JpaRepository<MaintenanceRate, Long> {

    /**
     * 1. Find a rate explicitly assigned to a specific flat.
     * Checks that the target date falls between effectiveFrom and effectiveTo.
     * Handles the case where effectiveTo is NULL (meaning it's active indefinitely).
     */
    @Query("SELECT mr FROM MaintenanceRate mr WHERE mr.flat.id = :flatId AND mr.isActive = true " +
            "AND mr.effectiveFrom <= :targetDate AND (mr.effectiveTo IS NULL OR mr.effectiveTo >= :targetDate)")
    Optional<MaintenanceRate> findRateForFlat(
            @Param("flatId") Long flatId,
            @Param("targetDate") LocalDate targetDate);

    /**
     * 2. Find the default rate for the entire apartment (where flat is NULL).
     * Used as a fallback if no flat-specific rate exists for that period.
     */
    @Query("SELECT mr FROM MaintenanceRate mr WHERE mr.apartment.id = :apartmentId AND mr.flat IS NULL AND mr.isActive = true " +
            "AND mr.effectiveFrom <= :targetDate AND (mr.effectiveTo IS NULL OR mr.effectiveTo >= :targetDate)")
    Optional<MaintenanceRate> findDefaultRateForApartment(
            @Param("apartmentId") Long apartmentId,
            @Param("targetDate") LocalDate targetDate);
}