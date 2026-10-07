package com.yesh.apartmentledger.finance.income.service;

import com.yesh.apartmentledger.core.enums.PaymentStatusEnum;
import com.yesh.apartmentledger.core.flat.dto.FlatPaymentStatusResponse;
import com.yesh.apartmentledger.core.flat.repository.FlatRepository;
import com.yesh.apartmentledger.exception.ResourceNotFoundException;
import com.yesh.apartmentledger.finance.income.dto.IncomeResponse;
import com.yesh.apartmentledger.finance.income.dto.PaymentModeIncomeSummary;
import com.yesh.apartmentledger.finance.income.entity.Income;
import com.yesh.apartmentledger.finance.income.repository.IncomeRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class IncomeService {

    private final IncomeRepository incomeRepository;
    private final FlatRepository flatRepository;

    /**
     * Fetch all amount received (income) for a specific period.
     * @param apartmentId The apartment id
     * @param year The year of the income period.
     * @param month the month of the income period
     * @return IncomeResponse
     */
    public List<IncomeResponse> getIncomeByApartmentYearAndMonth(Long apartmentId, Short year, Short month){
        var incomes =  incomeRepository.findByApartmentYearMonth(apartmentId,year,month);

        return incomes.stream().map(this::toResponse).toList();

    }

    /**
     * @param apartmentId The apartment id of the flat
     * @param flatId The unique id that represents a flat
     * @param year The year of the payment period
     * @param month The month of the payment period.
     * @return FlatPaymentStatusResponse
     */
    public FlatPaymentStatusResponse getFlatMaintenancePaymentStatus(Long apartmentId, Long flatId, Short year, Short month){
        var incomes =  incomeRepository.findByApartmentYearMonth(apartmentId,year,month);
        var status = PaymentStatusEnum.DUE.name();

        var flat = flatRepository.findById(flatId).orElseThrow( ()-> new ResourceNotFoundException("Flat not found"));
        var flatIncome = incomes.stream().filter(i-> i.getFlat().getId().equals(flatId)
                        && i.getLedgerCategory().getIsFlatMaintenance().equals(true))
                .findFirst().orElse(null);

        if (flatIncome != null)
        {
            status = PaymentStatusEnum.PAID.name();
        }

        return  new FlatPaymentStatusResponse(flat.getFlatNumber(), flat.getOwnerName(),year,month,status,
                (flatIncome == null ? BigDecimal.ZERO : flatIncome.getAmount()),
                (flatIncome == null ? null : flatIncome.getTransactionDate()),
                (flatIncome == null ? null : flatIncome.getPaymentMode().getPaymentModeName()),
                (flatIncome == null ? null : flatIncome.getReferenceNo()),
                (flatIncome == null? null : BigDecimal.ZERO ));

    }

    /**
     * @param apartmentId The apartment id
     * @param year The year of the income period.
     * @param month The month of the income period.
     * @return Payment mode wise income amount summary
     */
    public List<PaymentModeIncomeSummary> getIncomeSummaryByPaymentMode(Long apartmentId, Short year, Short month){
            return incomeRepository.sumIncomeByPaymentMode(apartmentId,year,month);

    }

    //=======================================
    // Private methods
    //=======================================
    private IncomeResponse toResponse(Income income) {
        return new IncomeResponse(income.getId(),
                    income.getTransactionDate(),
                income.getApartment().getApartmentName(),
                income.getFlat().getFlatNumber(),
                income.getFlat().getOwnerName(),
                income.getLedgerCategory().getCategoryName(),
                income.getLedgerCategory().getTransactionType(),
                income.getReferenceNo(),
                income.getRemarks(),
                income.getAmount(),
                income.getPaymentMode().getPaymentModeName()
                );
    }

}
