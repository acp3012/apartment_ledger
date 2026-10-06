package com.yesh.apartmentledger.finance.income.service;

import com.yesh.apartmentledger.finance.income.dto.IncomeResponse;
import com.yesh.apartmentledger.finance.income.dto.PaymentModeIncomeSummary;
import com.yesh.apartmentledger.finance.income.entity.Income;
import com.yesh.apartmentledger.finance.income.repository.IncomeRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class IncomeService {

    private final IncomeRepository incomeRepository;

    public List<IncomeResponse> getIncomeByApartmentYearAndMonth(Long apartmentId, Short year, Short month){
        var incomes =  incomeRepository.findByApartmentYearMonth(apartmentId,year,month);

        return incomes.stream().map(this::toResponse).toList();

    }

    public List<PaymentModeIncomeSummary> getIncomeSummaryByPaymentMode(Long apartmentId, Short year, Short month){
            return incomeRepository.sumIncomeByPaymentMode(apartmentId,year,month);

    }


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
