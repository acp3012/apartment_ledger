package com.yesh.apartmentledger.core.apartment.service;

import com.yesh.apartmentledger.core.apartment.dto.ApartmentActiveStatusRequest;
import com.yesh.apartmentledger.core.apartment.dto.ApartmentCreateRequest;
import com.yesh.apartmentledger.core.apartment.dto.ApartmentResponse;
import com.yesh.apartmentledger.core.apartment.entity.Apartment;
import com.yesh.apartmentledger.core.apartment.repository.ApartmentRepository;
import com.yesh.apartmentledger.exception.ResourceAlreadyExistsException;
import com.yesh.apartmentledger.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApartmentService {

    private final ApartmentRepository apartmentRepository;

    @Transactional
    public ApartmentResponse createApartment(ApartmentCreateRequest request) {
        if (apartmentRepository.existsByApartmentCode(request.apartmentCode())) {
            throw new ResourceAlreadyExistsException(
                    "Apartment code already exists: " + request.apartmentCode());
        }

        Apartment apartment = new Apartment();
        apartment.setApartmentCode(request.apartmentCode());
        apartment.setApartmentName(request.apartmentName());
        apartment.setAddress(request.address());
        apartment.setCity(request.city());
        apartment.setState(request.state());
        apartment.setPincode(request.pincode());
        apartment.setBankName(request.bankName());
        apartment.setAccountNumber(request.accountNumber());
        apartment.setIfscCode(request.ifscCode());
        apartment.setGoLiveYear(request.goLiveYear());
        apartment.setGoLiveMonth(request.goLiveMonth());
        apartment.setIsActive(true);

        return ApartmentResponse.from(apartmentRepository.save(apartment));
    }

    @Transactional
    public ApartmentResponse updateActiveStatus(Long apartmentId, ApartmentActiveStatusRequest request) {
        Apartment apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Apartment not found with id: " + apartmentId));
        apartment.setIsActive(request.isActive());

        return ApartmentResponse.from(apartmentRepository.save(apartment));
    }
    // getAllApartments method added to ApartmentService
    @Transactional(readOnly = true)
    public List<ApartmentResponse> getAllApartments() {
        return apartmentRepository.findAll().stream()
                .map(ApartmentResponse::from)
                .toList();
    }
    @Operation(summary = "Get an apartment by ID",
            description = "Returns the details of a specific apartment based on its ID.")   
    public ApartmentResponse getApartmentById(Long apartmentId) {
        Apartment apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Apartment not found with id: " + apartmentId));
        return ApartmentResponse.from(apartment);
    }
}