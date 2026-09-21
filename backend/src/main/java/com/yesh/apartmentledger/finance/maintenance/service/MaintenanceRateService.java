package com.yesh.apartmentledger.finance.maintenance.service;

import com.yesh.apartmentledger.exception.ResourceNotFoundException;
import com.yesh.apartmentledger.finance.maintenance.dto.MaintenanceRateResponse;
import com.yesh.apartmentledger.finance.maintenance.entity.MaintenanceRate;
import com.yesh.apartmentledger.finance.maintenance.repository.MaintenanceRateRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@AllArgsConstructor
public class MaintenanceRateService {

    private final MaintenanceRateRepository maintenanceRateRepository;

    //======================================================
    // Fetch maintenance fee for a specific period
    //=====================================================
    public MaintenanceRateResponse getMaintenanceFee(Long apartmentId, Long flatId, LocalDate targetDate) {

        // 1. Try to find the flat-specific rate
        // 2. If empty, fallback to the apartment-wide rate
        // 3. If STILL empty, throw an exception (or return 0 depending on your business rules)

        MaintenanceRate applicableRate = maintenanceRateRepository.findRateForFlat(flatId, targetDate)
                .orElseGet(() -> maintenanceRateRepository.findDefaultRateForApartment(apartmentId, targetDate)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "No active maintenance rate found for this date!")));

        return new MaintenanceRateResponse( apartmentId, flatId, applicableRate.getFee()
        );

    }
}
