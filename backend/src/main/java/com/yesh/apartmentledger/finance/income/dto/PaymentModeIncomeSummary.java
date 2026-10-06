package com.yesh.apartmentledger.finance.income.dto;

import java.math.BigDecimal;

public record PaymentModeIncomeSummary(String paymentModeName, BigDecimal incomeAmount, Long transactionCount,
                                       Short year, Short month, Long apartmentId) {}
