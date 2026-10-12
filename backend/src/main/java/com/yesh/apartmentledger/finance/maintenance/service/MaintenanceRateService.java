package com.yesh.apartmentledger.finance.maintenance.service;

import com.yesh.apartmentledger.core.apartment.entity.Apartment;
import com.yesh.apartmentledger.core.apartment.repository.ApartmentRepository;
import com.yesh.apartmentledger.core.flat.entity.Flat;
import com.yesh.apartmentledger.core.flat.repository.FlatRepository;
import com.yesh.apartmentledger.core.user.entity.AppUser;
import com.yesh.apartmentledger.core.user.repository.AppUserRepository;
import com.yesh.apartmentledger.exception.BadRequestException;
import com.yesh.apartmentledger.exception.ResourceNotFoundException;
import com.yesh.apartmentledger.finance.maintenance.dto.MaintenanceRateRequest;
import com.yesh.apartmentledger.finance.maintenance.dto.MaintenanceRateResponse;
import com.yesh.apartmentledger.finance.maintenance.entity.MaintenanceRate;
import com.yesh.apartmentledger.finance.maintenance.repository.MaintenanceRateRepository;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@AllArgsConstructor
public class MaintenanceRateService {

    private final MaintenanceRateRepository maintenanceRateRepository;
    private final ApartmentRepository apartmentRepository;
    private final FlatRepository flatRepository;
    private final AppUserRepository appUserRepository;

    // Create a new maintenance fee for an apartment or a specific flat within an apartment. If flatId is provided, it will be a flat-specific rate; otherwise, it will be an apartment-wide rate.
    @Transactional
    public MaintenanceRateResponse createMaintenanceFee(Long apartmentId,  Long flatId, MaintenanceRateRequest request) {

          
        Apartment apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Apartment not found with id: " + apartmentId));
        Flat flat = null;
        if (flatId != null) {
            flat = flatRepository.findByIdAndApartmentId(flatId, apartmentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Flat not found with id: " + flatId)); 
            
        } 

        if (hasOverlappingRate(apartmentId, request.effectiveFrom(), request.effectiveTo())) {
            throw new BadRequestException("A maintenance rate already exists for this apartment within the selected effective date range.");
        }
        AppUser createdByUser = appUserRepository.findById(request.createdBy())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.createdBy()));
        MaintenanceRate maintenanceRate = new MaintenanceRate();
        maintenanceRate.setFlat(flat == null ? null : flat);
        maintenanceRate.setApartment(apartment);
        maintenanceRate.setEffectiveFrom(request.effectiveFrom());
        maintenanceRate.setEffectiveTo(request.effectiveTo());
        maintenanceRate.setFee(request.fee());
        maintenanceRate.setCreatedBy(createdByUser);
        maintenanceRate.setIsActive(true);

        maintenanceRate = maintenanceRateRepository.save(maintenanceRate);

        return new MaintenanceRateResponse(
                maintenanceRate.getId(),
                apartmentId,
                flatId == null ? null : flatId,
                maintenanceRate.getEffectiveFrom(),
                maintenanceRate.getEffectiveTo(),
                maintenanceRate.getFee(),
                maintenanceRate.getCreatedDate(),
                maintenanceRate.getUpdatedDate()
        );
    }


    //======================================================
    // Fetch maintenance fee for a specific period
    //=====================================================
   
    public MaintenanceRateResponse getMaintenanceFee(Long apartmentId, Long flatId, LocalDate targetDate) {

        MaintenanceRate applicableRate = maintenanceRateRepository.findRateForFlat(flatId, targetDate)
                .orElseGet(() -> maintenanceRateRepository.findDefaultRateForApartment(apartmentId, targetDate)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "No active maintenance rate found for this date!")));

        return new MaintenanceRateResponse(
                applicableRate.getId(),
                applicableRate.getApartment().getId(),
                applicableRate.getFlat() != null ? applicableRate.getFlat().getId() : null,
                applicableRate.getEffectiveFrom(),
                applicableRate.getEffectiveTo(),
                applicableRate.getFee(),
                applicableRate.getCreatedDate(),
                applicableRate.getUpdatedDate()
        );
    }

    //======================================================
    // Private helper methods
    //======================================================
    private boolean hasOverlappingRate(Long apartmentId, LocalDate effectiveFrom, LocalDate effectiveTo) {
        return maintenanceRateRepository
            .findDefaultRateForApartment(apartmentId, effectiveFrom).isPresent()
                || maintenanceRateRepository
                .findDefaultRateForApartment(apartmentId, effectiveTo).isPresent();
    }

}
