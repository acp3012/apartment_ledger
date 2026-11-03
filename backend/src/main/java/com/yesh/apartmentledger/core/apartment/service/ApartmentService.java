package com.yesh.apartmentledger.core.apartment.service;

import com.yesh.apartmentledger.core.apartment.dto.ApartmentActiveStatusRequest;
import com.yesh.apartmentledger.core.apartment.dto.ApartmentCreateRequest;
import com.yesh.apartmentledger.core.apartment.dto.ApartmentResponse;
import com.yesh.apartmentledger.core.apartment.dto.GoLivePeriodRequest;
import com.yesh.apartmentledger.core.apartment.entity.Apartment;
import com.yesh.apartmentledger.core.apartment.repository.ApartmentRepository;
import com.yesh.apartmentledger.core.flat.repository.FlatRepository;
import com.yesh.apartmentledger.exception.ResourceAlreadyExistsException;
import com.yesh.apartmentledger.exception.ResourceNotFoundException;
import com.yesh.apartmentledger.exception.BadRequestException;
import com.yesh.apartmentledger.finance.expense.repository.ExpenseDraftRepository;
import com.yesh.apartmentledger.finance.expense.repository.ExpenseRepository;
import com.yesh.apartmentledger.finance.income.repository.IncomeDraftRepository;
import com.yesh.apartmentledger.finance.income.repository.IncomeRepository;
import com.yesh.apartmentledger.finance.ledger.repository.MonthlyLedgerRepository;
import com.yesh.apartmentledger.finance.ledger.entity.LedgerPeriod;
import com.yesh.apartmentledger.finance.ledger.repository.LedgerPeriodRepository;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApartmentService {

    private final ApartmentRepository apartmentRepository;
    private final FlatRepository flatRepository;
    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;
    private final IncomeDraftRepository incomeDraftRepository;
    private final ExpenseDraftRepository expenseDraftRepository;
    private final MonthlyLedgerRepository monthlyLedgerRepository;
    private final LedgerPeriodRepository ledgerPeriodRepository;

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

    @Transactional
    public ApartmentResponse updateGoLivePeriod(Long apartmentId, GoLivePeriodRequest request) {
        Apartment apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Apartment not found with id: " + apartmentId));

        if (Boolean.TRUE.equals(apartment.getIsSetupCompleted())) {
            throw new BadRequestException(
                    "The one-time setup has been completed and the starting period is locked.");
        }

        if (monthlyLedgerRepository.existsByApartment_Id(apartmentId)
                || incomeRepository.existsByApartment_Id(apartmentId)
                || expenseRepository.existsByApartment_Id(apartmentId)
                || incomeDraftRepository.existsByApartment_Id(apartmentId)
                || expenseDraftRepository.existsByApartment_Id(apartmentId)) {
            throw new BadRequestException(
                    "The go-live period cannot be changed after financial entries have been recorded.");
        }

        apartment.setGoLiveYear(request.year());
        apartment.setGoLiveMonth(request.month());

        LedgerPeriod activePeriod = ledgerPeriodRepository.findById(apartmentId)
                .orElseGet(() -> {
                    LedgerPeriod newPeriod = new LedgerPeriod();
                    newPeriod.setApartmentId(apartmentId);
                    return newPeriod;
                });
        activePeriod.setActiveYear(request.year());
        activePeriod.setActiveMonth(request.month());
        ledgerPeriodRepository.save(activePeriod);

        return ApartmentResponse.from(apartmentRepository.save(apartment));
    }

    @Transactional
    public ApartmentResponse completeInitialSetup(Long apartmentId) {
        Apartment apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Apartment not found with id: " + apartmentId));

        if (Boolean.TRUE.equals(apartment.getIsSetupCompleted())) {
            return ApartmentResponse.from(apartment);
        }

        if (!flatRepository.existsByApartment_Id(apartmentId)) {
            throw new BadRequestException("Create at least one flat before completing one-time setup.");
        }
        if (apartment.getGoLiveYear() == null || apartment.getGoLiveMonth() == null) {
            throw new BadRequestException(
                    "Set the financial starting period before completing one-time setup.");
        }

        apartment.setIsSetupCompleted(true);
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