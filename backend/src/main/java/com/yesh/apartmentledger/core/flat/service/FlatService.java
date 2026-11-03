package com.yesh.apartmentledger.core.flat.service;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;

import com.yesh.apartmentledger.core.apartment.entity.Apartment;
import com.yesh.apartmentledger.core.apartment.repository.ApartmentRepository;
import com.yesh.apartmentledger.core.flat.dto.FlatRequest;
import com.yesh.apartmentledger.core.flat.dto.FlatResponse;
import com.yesh.apartmentledger.core.flat.repository.FlatRepository;
import com.yesh.apartmentledger.exception.ResourceAlreadyExistsException;
import com.yesh.apartmentledger.exception.ResourceNotFoundException;
import com.yesh.apartmentledger.exception.BadRequestException;

import com.yesh.apartmentledger.core.flat.entity.Flat;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor 
public class FlatService {

    private final FlatRepository flatRepository;
    private final ApartmentRepository apartmentRepository;
    
    public List<FlatResponse> getFlatsByApartment(Long apartmentId) {
        return flatRepository.findByApartmentId(apartmentId)
                .stream()
                .map(this::mapToFlatResponse)
                .toList();
        
    }

    @Transactional
    public FlatResponse createFlat(Long apartmentId, FlatRequest flatRequest) {
        var apartment = getApartmentForSetup(apartmentId);
        
        validateFlatRequest(apartmentId, flatRequest);
        
         var newFlat = getNewFlat(flatRequest, apartment);
        
        flatRepository.save(newFlat);
        
        return mapToFlatResponse(newFlat);
    }

    @Transactional
    public FlatResponse updateFlat(Long apartmentId, Long flatId, FlatRequest flatRequest) {
        var flat = flatRepository.findByIdAndApartmentId(flatId, apartmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Flat not found"));
        validateFlatRequest(apartmentId, flatRequest, flatId);

        flat.setFlatNumber(flatRequest.flatNumber());
        flat.setFloorNumber(flatRequest.floorNumber());
        flat.setOwnerName(flatRequest.ownerName());
        flat.setOwnerMobile(flatRequest.ownerMobileNumber());
        flat.setOwnerEmail(flatRequest.ownerEmail());

        return mapToFlatResponse(flatRepository.save(flat));
    }
    
    //===================================
    // private methods
    //===================================
    private Flat getNewFlat(FlatRequest flatRequest, Apartment apartment) {
        var newFlat = new Flat();
        newFlat.setFlatNumber(flatRequest.flatNumber());
        newFlat.setFloorNumber(flatRequest.floorNumber());
        newFlat.setOwnerName(flatRequest.ownerName());
        newFlat.setOwnerMobile(flatRequest.ownerMobileNumber());
        newFlat.setOwnerEmail(flatRequest.ownerEmail());    
        newFlat.setMaintenanceAmount(flatRequest.maintenanceFee() != null ? flatRequest.maintenanceFee() : new BigDecimal("0.00"));
        newFlat.setApartment(apartment);
        return newFlat;
    }

    private FlatResponse mapToFlatResponse(Flat flat) {
        return new FlatResponse(
            flat.getApartment().getId(),
            flat.getId(),
            flat.getFloorNumber(),
            flat.getFlatNumber(),
            flat.getOwnerName(),
            flat.getOwnerMobile(),
            flat.getOwnerEmail()
        );
    }

    private void validateFlatRequest(Long apartmentId, FlatRequest flatRequest) {
        flatRepository.findByFlatNumberAndApartmentId(flatRequest.flatNumber(), apartmentId)
                .ifPresent(flat -> {
                    throw new ResourceAlreadyExistsException("Flat number already exists for this apartment");
                });
        flatRepository.findByOwnerMobileAndApartmentId(flatRequest.ownerMobileNumber(), apartmentId)
                .ifPresent(flat -> {
                    throw new ResourceAlreadyExistsException("Owner mobile number already exists for this apartment");
                });
    }

    private void validateFlatRequest(Long apartmentId, FlatRequest flatRequest, Long flatId) {
        flatRepository.findByFlatNumberAndApartmentId(flatRequest.flatNumber(), apartmentId)
                .filter(flat -> !flat.getId().equals(flatId))
                .ifPresent(flat -> {
                    throw new ResourceAlreadyExistsException("Flat number already exists for this apartment");
                });
        flatRepository.findByOwnerMobileAndApartmentId(flatRequest.ownerMobileNumber(), apartmentId)
                .filter(flat -> !flat.getId().equals(flatId))
                .ifPresent(flat -> {
                    throw new ResourceAlreadyExistsException("Owner mobile number already exists for this apartment");
                });
    }

    private Apartment getApartmentForSetup(Long apartmentId) {
        var apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Apartment not found"));
        if (Boolean.TRUE.equals(apartment.getIsSetupCompleted())) {
            throw new BadRequestException(
                    "The one-time setup has been completed and flat creation is locked.");
        }
        return apartment;
    }
}
