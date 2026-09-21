package com.yesh.apartmentledger.core.validation;

import com.yesh.apartmentledger.core.apartment.entity.Apartment;
import com.yesh.apartmentledger.exception.BadRequestException;
import com.yesh.apartmentledger.finance.ledger.repository.MonthlyLedgerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LedgerValidationUtil {

    private final MonthlyLedgerRepository monthlyLedgerRepository;

    /**
     * Ensures transactions are valid based on Go-Live date AND Month-End closures.
     */
    public void validateBillingPeriod(Apartment apartment, Short txnYear, Short txnMonth) {

        // 1. GO-LIVE RULE: Cannot post before the system went live
        if (apartment.getGoLiveYear() != null && apartment.getGoLiveMonth() != null) {
            if (txnYear < apartment.getGoLiveYear() ||
                    (txnYear.equals(apartment.getGoLiveYear()) && txnMonth < apartment.getGoLiveMonth())) {
                throw new BadRequestException(
                        "Transactions are locked for periods before the system Go-Live date ("
                                + apartment.getGoLiveMonth() + "/" + apartment.getGoLiveYear() + ")."
                );
            }
        }

        // 2. VAULT DOOR RULE: Cannot post if the month is already closed
        boolean isMonthClosed = monthlyLedgerRepository.existsByApartmentIdAndYearAndMonthAndStatus(
                apartment.getId(), txnYear, txnMonth,"CLOSED");

        if (isMonthClosed) {
            throw new BadRequestException(
                    "The ledger for " + txnMonth + "/" + txnYear + " is already closed. " +
                            "No further transactions can be drafted or approved for this period."
            );
        }
    }
}