package com.yesh.apartmentledger.master.paymentmode.service;

import com.yesh.apartmentledger.exception.ResourceNotFoundException;
import com.yesh.apartmentledger.master.cache.LookupCacheService;
import com.yesh.apartmentledger.master.paymentmode.entity.PaymentMode;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class PaymentModeService {
    private final LookupCacheService lookupCacheService;

    public PaymentMode getPaymentModeByName(String paymentModeName) {
        PaymentMode paymentMode = lookupCacheService.getPaymentModeByName(paymentModeName);
        if (paymentMode == null) {
            throw new ResourceNotFoundException("Payment mode not found for :" + paymentModeName);
        }
        return paymentMode;
    }
    public List<PaymentMode> getAllActivePaymentModes(){
        return lookupCacheService.getPaymentModes().stream()
                .filter(paymentMode -> Boolean.TRUE.equals(paymentMode.getIsActive()))
            .sorted((left, right) -> Integer.compare(left.getDisplayOrder(), right.getDisplayOrder()))
                .toList();
    }
}
