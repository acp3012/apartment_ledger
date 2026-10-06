package com.yesh.apartmentledger.core.validation;

import com.yesh.apartmentledger.core.apartment.entity.Apartment;
import com.yesh.apartmentledger.core.apartment.repository.ApartmentRepository;
import com.yesh.apartmentledger.core.user.entity.AppUser;
import com.yesh.apartmentledger.core.user.repository.AppUserRepository;
import com.yesh.apartmentledger.exception.BadRequestException;
import com.yesh.apartmentledger.exception.ResourceNotFoundException;
import com.yesh.apartmentledger.master.ledgercategory.entity.LedgerCategory;
import com.yesh.apartmentledger.master.ledgercategory.repository.LedgerCategoryRepository;
import com.yesh.apartmentledger.master.paymentmode.entity.PaymentMode;
import com.yesh.apartmentledger.master.paymentmode.repository.PaymentModeRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public  class MasterEntityValidator {
    private final AppUserRepository appUserRepository;
    private final LedgerCategoryRepository ledgerCategoryRepository;
    private final PaymentModeRepository paymentModeRepository;
    private final ApartmentRepository apartmentRepository;

    ///
    /// @param apartmentId - Apartment
    /// @param ledgerCategoryId Ledger Category
    /// @param paymentModeId Payment Mode
    /// @param userId The user
    /// @return Apartment, LedgerCategory, PaymentMode, User Entities
    public  EntityResult validateAndGetEntities(@Nullable Long apartmentId, Long ledgerCategoryId, Long paymentModeId, Long userId) {
        var apartment = new Apartment();

        if(apartmentId != null) {
            apartment = apartmentRepository.findById(apartmentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Apartment id [" + apartmentId + " ] Not found"));
        }
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        LedgerCategory ledgerCategory = ledgerCategoryRepository.findById(ledgerCategoryId)
                .orElseThrow(()-> new ResourceNotFoundException("Ledger category not found for [ "+ledgerCategoryId + " ]"));

        PaymentMode paymentMode = paymentModeRepository.findById(paymentModeId)
                .orElseThrow(()-> new ResourceNotFoundException("Payment mode not found for [ "+paymentModeId + " ]"));

        return new EntityResult(apartment, ledgerCategory, paymentMode,user);

    }

    public record EntityResult(Apartment apartment, LedgerCategory ledgerCategory, PaymentMode paymentMode, AppUser user)
    {  }

    /// Validate ledger category transaction type
    /// @param givenCode The transaction type from request
    /// @param toBe  Expected transaction type.
    public void throwErrorIfLedgerCategoryCodeNotMatch(String givenCode, String toBe) {
        if(!givenCode.equalsIgnoreCase(toBe) )
        {
            throw new BadRequestException("The ledger category transaction type must be " + toBe);
        }
    }
}
