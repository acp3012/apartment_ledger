package com.yesh.apartmentledger.finance.maintenance.service;

import com.yesh.apartmentledger.core.apartment.entity.Apartment;
import com.yesh.apartmentledger.core.apartment.repository.ApartmentRepository;
import com.yesh.apartmentledger.core.flat.entity.Flat;
import com.yesh.apartmentledger.core.flat.repository.FlatRepository;
import com.yesh.apartmentledger.core.user.entity.AppUser;
import com.yesh.apartmentledger.exception.BadRequestException;
import com.yesh.apartmentledger.exception.ResourceNotFoundException;
import com.yesh.apartmentledger.finance.maintenance.dto.MaintenanceRateRequest;
import com.yesh.apartmentledger.finance.maintenance.dto.MaintenanceRateResponse;
import com.yesh.apartmentledger.finance.maintenance.entity.MaintenanceRate;
import com.yesh.apartmentledger.finance.maintenance.repository.MaintenanceRateRepository;
import com.yesh.apartmentledger.security.service.CurrentUserService;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@AllArgsConstructor
public class MaintenanceRateService {

    private final MaintenanceRateRepository maintenanceRateRepository;
    private final ApartmentRepository apartmentRepository;
    private final FlatRepository flatRepository;
    private final CurrentUserService currentUserService;

    // Create a new maintenance fee for an apartment or a specific flat within an apartment. If flatId is provided, it will be a flat-specific rate; otherwise, it will be an apartment-wide rate.
    @Transactional
    public MaintenanceRateResponse createMaintenanceFee(Long apartmentId,  Long flatId, MaintenanceRateRequest request) {
        currentUserService.requireApartmentAccess(apartmentId);

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
        MaintenanceRate maintenanceRate = getMaintenanceRate(request, flat, apartment);

        maintenanceRate = maintenanceRateRepository.save(maintenanceRate);

        return new MaintenanceRateResponse(
                maintenanceRate.getId(),
                apartmentId,
                flatId,
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
        currentUserService.requireApartmentAccess(apartmentId);
        flatRepository.findByIdAndApartmentId(flatId, apartmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Flat not found with id: " + flatId));

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
    private @NonNull MaintenanceRate getMaintenanceRate(MaintenanceRateRequest request, Flat flat, Apartment apartment) {
        AppUser createdByUser = currentUserService.getCurrentUser();
        MaintenanceRate maintenanceRate = new MaintenanceRate();
        maintenanceRate.setFlat(flat);
        maintenanceRate.setApartment(apartment);
        maintenanceRate.setEffectiveFrom(request.effectiveFrom());
        maintenanceRate.setEffectiveTo(request.effectiveTo());
        maintenanceRate.setFee(request.fee());
        maintenanceRate.setCreatedBy(createdByUser);
        maintenanceRate.setIsActive(true);
        return maintenanceRate;
    }


}
