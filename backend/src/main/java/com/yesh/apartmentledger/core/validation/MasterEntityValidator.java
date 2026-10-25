package com.yesh.apartmentledger.core.validation;

import com.yesh.apartmentledger.exception.BadRequestException;
import com.yesh.apartmentledger.exception.ResourceNotFoundException;
import com.yesh.apartmentledger.master.cache.LookupCacheService;
import com.yesh.apartmentledger.master.ledgercategory.entity.LedgerCategory;
import com.yesh.apartmentledger.master.paymentmode.entity.PaymentMode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public  class MasterEntityValidator {
    private final LookupCacheService lookupCacheService;

    ///
    /// @param ledgerCategoryId Ledger Category
    /// @param paymentModeId Payment Mode
        /// @return LedgerCategory and PaymentMode entities
        public EntityResult validateAndGetEntities(Long ledgerCategoryId, Long paymentModeId) {
        LedgerCategory ledgerCategory = lookupCacheService.getLedgerCategoryById(ledgerCategoryId);
        if (ledgerCategory == null) {
            throw new ResourceNotFoundException("Ledger category not found for [ " + ledgerCategoryId + " ]");
        }

        PaymentMode paymentMode = lookupCacheService.getPaymentModeById(paymentModeId);
        if (paymentMode == null) {
            throw new ResourceNotFoundException("Payment mode not found for [ " + paymentModeId + " ]");
        }

        return new EntityResult(ledgerCategory, paymentMode);

    }
    public record EntityResult(LedgerCategory ledgerCategory, PaymentMode paymentMode) {}

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
