package com.yesh.apartmentledger.core.flat.dto;

import java.math.BigDecimal;

import java.util.List;

public record FlatPaymentStatusResponse(
        Long apartmentId,
        Long flatId,
        String flatNumber,
        String ownerName,
        Short year,
        Short month,
        BigDecimal monthlyFee,
        BigDecimal totalAmountPaid,
        String status,
        BigDecimal adjustmentAmount, // either DUE or Advance
        List<PaymentReference> paymentReferenceList

) {}

