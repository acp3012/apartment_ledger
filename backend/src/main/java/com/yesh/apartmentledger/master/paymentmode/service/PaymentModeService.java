package com.yesh.apartmentledger.master.paymentmode.service;

import com.yesh.apartmentledger.exception.ResourceNotFoundException;
import com.yesh.apartmentledger.master.paymentmode.entity.PaymentMode;
import com.yesh.apartmentledger.master.paymentmode.repository.PaymentModeRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class PaymentModeService {
    private final PaymentModeRepository paymentModeRepository;

    public PaymentMode getPaymentModeByName(String paymentModeName) {
        Optional<PaymentMode> paymentMode = Optional.of(paymentModeRepository.findByPaymentModeNameIgnoreCase(paymentModeName)
                .orElseThrow(() -> new ResourceNotFoundException("Payment mode not found for :" + paymentModeName)));
        return paymentMode.get();
    }
    public List<PaymentMode> getAllActivePaymentModes(){
        return paymentModeRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
    }
}
