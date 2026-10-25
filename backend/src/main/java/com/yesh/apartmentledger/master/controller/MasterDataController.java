package com.yesh.apartmentledger.master.controller;
import com.yesh.apartmentledger.master.ledgercategory.dto.LedgerCategoryResponse;
import com.yesh.apartmentledger.master.ledgercategory.entity.LedgerCategory;
import com.yesh.apartmentledger.master.cache.LookupCacheService;
import com.yesh.apartmentledger.master.paymentmode.dto.PaymentModeResponse;
import com.yesh.apartmentledger.master.paymentmode.entity.PaymentMode;
import com.yesh.apartmentledger.master.paymentmode.service.PaymentModeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/master")
@RequiredArgsConstructor
public class MasterDataController {

    private final PaymentModeService paymentModeService;
        private final LookupCacheService lookupCacheService;

    //-------------------------------------------
    // Payment Mode
    //-------------------------------------------
    @GetMapping("/payment-modes")
    public List<PaymentModeResponse> getPaymentModes() {

        List<PaymentMode> paymentModes = paymentModeService.getAllActivePaymentModes();

        return paymentModes.stream()
                .map(mode -> new PaymentModeResponse(
                        mode.getId(),
                        mode.getPaymentModeName(),
                        mode.getIsActive(),
                        mode.getDisplayOrder()
                ))
                .toList();
    }

    @GetMapping("/payment-modes/{paymentModeName}")
    public ResponseEntity<PaymentMode> getActivePaymentModesByName(@PathVariable String paymentModeName) {
        return ResponseEntity.ok(paymentModeService.getPaymentModeByName(paymentModeName));
    }

    //-------------------------------------------
    // Ledger Category
    //-------------------------------------------
    @GetMapping("/ledger-categories")
    public List<LedgerCategoryResponse> getAllLedgerCategories() {
        List<LedgerCategory> ledgerCategories = lookupCacheService.getLedgerCategories();
        return toLedgerCategoryResponse(ledgerCategories);

    }

    @GetMapping("/ledger-categories/{transactionType}")
    public List<LedgerCategoryResponse> getLedgerCategoriesByTransactionType(@PathVariable String transactionType) {
        List<LedgerCategory> ledgerCategories = lookupCacheService.getLedgerCategories().stream()
                .filter(category -> category.getTransactionType().equalsIgnoreCase(transactionType))
                .toList();
        return toLedgerCategoryResponse(ledgerCategories);

    }


    private List<LedgerCategoryResponse> toLedgerCategoryResponse(List<LedgerCategory> ledgerCategories){
         return ledgerCategories.stream()
                 .map(category -> new LedgerCategoryResponse(
                         category.getId(),
                         category.getCategoryName(),
                         category.getTransactionType(),
                         category.getIsFlatMaintenance(),
                         category.getIsActive(),
                         category.getDisplayOrder()
                 ) ).toList();
    }

}
