package com.yesh.apartmentledger.finance.income.service;

import com.yesh.apartmentledger.core.enums.PaymentStatusEnum;
import com.yesh.apartmentledger.core.flat.dto.FlatPaymentStatusResponse;
import com.yesh.apartmentledger.core.flat.dto.PaymentReference;
import com.yesh.apartmentledger.core.flat.entity.Flat;
import com.yesh.apartmentledger.core.flat.repository.FlatRepository;
import com.yesh.apartmentledger.exception.ResourceNotFoundException;
import com.yesh.apartmentledger.finance.income.dto.IncomeResponse;
import com.yesh.apartmentledger.finance.income.dto.PaymentModeIncomeSummary;
import com.yesh.apartmentledger.finance.income.entity.Income;
import com.yesh.apartmentledger.finance.income.repository.IncomeRepository;
import com.yesh.apartmentledger.finance.maintenance.service.MaintenanceRateService;
import com.yesh.apartmentledger.security.service.CurrentUserService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
@Log4j2
@Service
@AllArgsConstructor
public class IncomeService {

    private final IncomeRepository incomeRepository;
    private final FlatRepository flatRepository;
    private final CurrentUserService currentUserService;
    private final MaintenanceRateService maintenanceRateService;
    /**
     * Fetch all amount received (income) for a specific period.
     * @param apartmentId The apartment id
     * @param year The year of the income period.
     * @param month the month of the income period
     * @return IncomeResponse
     */
    public List<IncomeResponse> getIncomeByApartmentYearAndMonth(Long apartmentId, Short year, Short month){
        currentUserService.requireApartmentAccess(apartmentId);
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
        currentUserService.requireApartmentAccess(apartmentId);

        var targetDate = LocalDate.of(year,month,1);

        Flat flat = flatRepository.findByIdAndApartmentId(flatId,apartmentId).orElseThrow(() -> new ResourceNotFoundException("Flat not found."));

        var incomes = incomeRepository.findByApartmentFlatYearMonth(apartmentId, flatId, year, month);

        BigDecimal monthlyFee = maintenanceRateService.getMaintenanceFee(apartmentId, flatId, targetDate).fee();

        log.info("Maintenance rate for flat {} on {} is {}",flatId,targetDate,monthlyFee);

        List<Income> maintenanceIncomes = incomes.stream()
                .filter(income -> Boolean.TRUE.equals(income.getLedgerCategory().getIsFlatMaintenance()))
                .toList();

        List<PaymentReference> paymentReferences = maintenanceIncomes.stream()
                .map(income-> new PaymentReference(
                        income.getId(),
                        income.getTransactionDate(),
                        income.getPaymentMode().getPaymentModeName(),
                        income.getReferenceNo(),
                        income.getRemarks(),
                        income.getAmount()
                        )).toList();

        BigDecimal paidAmount = maintenanceIncomes.stream()
                .map(Income::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String status;
        BigDecimal adjustAmount = paidAmount.subtract(monthlyFee);

        if(adjustAmount.compareTo(BigDecimal.ZERO) < 0){
            status = PaymentStatusEnum.DUE.name();
            adjustAmount = adjustAmount.abs();
        }
        else {
            status = PaymentStatusEnum.PAID.name();
        }
       
        return new FlatPaymentStatusResponse(
                apartmentId,
                flat.getId(),
                flat.getFlatNumber(),
                flat.getOwnerName(),
                year,
                month,
                monthlyFee,
                paidAmount,
                status,
                adjustAmount,
                paymentReferences
        );
    }

    /**
     * @param apartmentId The apartment id
     * @param year The year of the income period.
     * @param month The month of the income period.
     * @return Payment mode wise income amount summary
     */
    public List<PaymentModeIncomeSummary> getIncomeSummaryByPaymentMode(Long apartmentId, Short year, Short month){
        currentUserService.requireApartmentAccess(apartmentId);
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
