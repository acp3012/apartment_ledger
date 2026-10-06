package com.yesh.apartmentledger.finance.ledger.service;

import com.yesh.apartmentledger.core.apartment.entity.Apartment;
import com.yesh.apartmentledger.core.apartment.repository.ApartmentRepository;
import com.yesh.apartmentledger.exception.BadRequestException;
import com.yesh.apartmentledger.exception.ResourceNotFoundException;
import com.yesh.apartmentledger.finance.expense.repository.ExpenseRepository;
import com.yesh.apartmentledger.finance.income.repository.IncomeRepository;
import com.yesh.apartmentledger.finance.ledger.dto.LedgerPeriodResponse;
import com.yesh.apartmentledger.finance.ledger.dto.LedgerSummaryResponse;
import com.yesh.apartmentledger.finance.ledger.dto.MonthEndCloseResponse;
import com.yesh.apartmentledger.finance.ledger.entity.MonthlyLedger;
import com.yesh.apartmentledger.finance.ledger.repository.LedgerDetailRepository;
import com.yesh.apartmentledger.finance.ledger.repository.LedgerPeriodRepository;
import com.yesh.apartmentledger.finance.ledger.repository.MonthlyLedgerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Optional;
// ... imports

@Service
@RequiredArgsConstructor
public class LedgerService {
    private final LedgerPeriodRepository ledgerPeriodRepository;
    private final MonthlyLedgerRepository monthlyLedgerRepository;
    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;
    private final ApartmentRepository apartmentRepository;

    // Current active period of the ledger
    public LedgerPeriodResponse getLedgerPeriod(Long apartmentId){
        var ledgerPeriod = ledgerPeriodRepository.findById(apartmentId)
                .orElseThrow(()-> new ResourceNotFoundException("Apartment id not found"));
        return new LedgerPeriodResponse(apartmentId, ledgerPeriod.getActiveYear(), ledgerPeriod.getActiveMonth(),ledgerPeriod.getMonthName());
    }

    // Get Ledger summary (Previous month Opening Balance, current month income & expense.

    @Transactional(readOnly = true)
    /*
     * Returns total income and total expense for an apartment.
     * Year and month are optional; pass null to include all.
     *
     * @param apartmentId apartment to summarise
     * @param year        filter by year, or null for all years
     * @param month       filter by month (1-12), or null for all months
     * @return summary with totalIncome and totalExpense (zero if no transactions)
     */
     public LedgerSummaryResponse getSummary(Long apartmentId, Short year, Short month) {
        // Get Previous month closing balance , current month's OB
        BigDecimal openingBalance = getOpeningBalance(apartmentId, year, month);


        BigDecimal totalIncome = Optional.ofNullable(incomeRepository.getTotalIncomeForMonth(apartmentId, year, month))
                .orElse(BigDecimal.ZERO);

        BigDecimal totalExpense = Optional.ofNullable(expenseRepository.getTotalExpenseForMonth(apartmentId, year, month))
                .orElse(BigDecimal.ZERO);

        BigDecimal closingBalance = openingBalance.add(totalIncome).subtract(totalExpense);



        return new LedgerSummaryResponse(
                apartmentId,
                year,
                month,
                openingBalance,
                totalIncome,
                totalExpense,
                closingBalance);

    }
        // ==========================================
        // 1. PREVIEW THE MONTH (Calculate dynamically)
        // ==========================================
        @Transactional(readOnly = true)
        public MonthEndCloseResponse previewMonthEndClose (Long apartmentId, Short year, Short month){

            // Check if already closed
            Optional<MonthlyLedger> existingLedger = monthlyLedgerRepository
                    .findByApartmentIdAndYearAndMonth(apartmentId, year, month);
            if (existingLedger.isPresent()) {
                MonthlyLedger ledger = existingLedger.get();
                return new MonthEndCloseResponse(
                        apartmentId, year, month,
                        ledger.getOpeningBalance(),
                        ledger.getIncome(),
                        ledger.getExpense(),
                        ledger.getClosingBalance(),
                        true // Tells UI to disable the "Submit" button
                );
            }

            // Calculate Previous Month/Year to get Opening Balance
            Short prevMonth = (short) YearMonth.of(year, month).plusMonths(1).getMonthValue();
            Short prevYear = (short) YearMonth.of(year, month).plusMonths(1).getYear();

            BigDecimal openingBalance = monthlyLedgerRepository.findByApartmentIdAndYearAndMonth(apartmentId, prevYear, prevMonth)
                    .map(MonthlyLedger::getClosingBalance)
                    .orElse(BigDecimal.ZERO); // If no previous ledger, assume 0 (or fetch manual go-live setup)

            // Sum Income & Expenses (Handle nulls if there are no transactions)
            BigDecimal totalIncome = Optional.ofNullable(incomeRepository.getTotalIncomeForMonth(apartmentId, year, month))
                    .orElse(BigDecimal.ZERO);

            BigDecimal totalExpense = Optional.ofNullable(expenseRepository.getTotalExpenseForMonth(apartmentId, year, month))
                    .orElse(BigDecimal.ZERO);

            BigDecimal computedClosingBalance = openingBalance.add(totalIncome).subtract(totalExpense);

            return new MonthEndCloseResponse(
                    apartmentId, year, month, openingBalance, totalIncome, totalExpense, computedClosingBalance, false
            );
        }

        // ==========================================
        // 2. COMMIT THE MONTH (Lock the Vault)
        // ==========================================
        @Transactional
        public String closeMonth (Long apartmentId, Short year, Short month){

            // Prevent duplicate closure
            if (monthlyLedgerRepository.findByApartmentIdAndYearAndMonth(apartmentId, year, month).isPresent()) {
                throw new BadRequestException("This month is already closed and cannot be modified.");
            }

            Apartment apartment = apartmentRepository.findById(apartmentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Apartment not found"));

            // Use the preview logic to get the final trusted numbers
            MonthEndCloseResponse snapshot = previewMonthEndClose(apartmentId, year, month);

            MonthlyLedger ledger = new MonthlyLedger();
            ledger.setApartment(apartment);
            ledger.setYear(year);
            ledger.setMonth(month);
            ledger.setOpeningBalance(snapshot.openingBalance());
            ledger.setIncome(snapshot.totalIncome());
            ledger.setExpense(snapshot.totalExpense());

            // We do NOT set closingBalance, the database does it!

            monthlyLedgerRepository.save(ledger);

            return "Successfully closed ledger for " + month + "/" + year;
        }

    // GET opening balance
    private BigDecimal getOpeningBalance(Long apartmentId,Short year, Short month){

        BigDecimal openingBalance = BigDecimal.ZERO;

        var previousYear = (short) YearMonth.of(year,month).minusMonths(1).getYear();
        var previousMonth = (short) YearMonth.of(year,month).minusMonths(1).getMonthValue();
        Optional<MonthlyLedger> existingLedger = monthlyLedgerRepository
                .findByApartmentIdAndYearAndMonth(apartmentId, previousYear, previousMonth);
        if(existingLedger.isPresent()){
            openingBalance = existingLedger.get().getClosingBalance();
        }
        return openingBalance;
    }

}
