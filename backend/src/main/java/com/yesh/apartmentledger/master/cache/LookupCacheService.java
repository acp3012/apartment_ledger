package com.yesh.apartmentledger.master.cache;

import com.yesh.apartmentledger.master.approvalstatus.entity.ApprovalStatus;
import com.yesh.apartmentledger.master.approvalstatus.repository.ApprovalStatusRepository;
import com.yesh.apartmentledger.master.ledgercategory.entity.LedgerCategory;
import com.yesh.apartmentledger.master.ledgercategory.repository.LedgerCategoryRepository;
import com.yesh.apartmentledger.master.paymentmode.entity.PaymentMode;
import com.yesh.apartmentledger.master.paymentmode.repository.PaymentModeRepository;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class LookupCacheService {
    private final ApprovalStatusRepository approvalStatusRepository;
    private final LedgerCategoryRepository ledgerCategoryRepository;
    private final PaymentModeRepository paymentModeRepository;

    // In-memory lookup storage
    private List<ApprovalStatus> cachedApprovalStatuses;
    private Map<Long, ApprovalStatus> approvalStatusMap;

    private List<LedgerCategory> cachedLedgerCategories;
    private Map<Long,LedgerCategory> cachedLedgerCategoryMap;

    private List<PaymentMode> cachedPaymentModes;
    private Map<Long,PaymentMode> cachedPaymentModeMap;

    // Load data automatically when the Spring Bean initializes
    @PostConstruct
    public void loadCache() {
        refreshCache();
    }

    // PUBLIC Methods
    public void refreshCache() {
        // Store Approval Statuses
        this.cachedApprovalStatuses = approvalStatusRepository.findAll();
        this.approvalStatusMap = cachedApprovalStatuses.stream()
                .collect(Collectors.toMap(ApprovalStatus::getId, status -> status));

        // Ledger Category
        this.cachedLedgerCategories = ledgerCategoryRepository.findAll();
        this.cachedLedgerCategoryMap = cachedLedgerCategories.stream()
                .collect(Collectors.toMap(LedgerCategory::getId,
                        category -> category));
        // Payment modes
        this.cachedPaymentModes = paymentModeRepository.findAll();
        this.cachedPaymentModeMap = cachedPaymentModes.stream()
                .collect(Collectors.
                        toMap(PaymentMode::getId,
                   paymentMode -> paymentMode));

        System.out.println("--> Lookup tables successfully loaded into memory cache!");
    }

    // Fast in-memory getters (No Database Hits!)
    public List<ApprovalStatus> getApprovalStatuses() {
        return cachedApprovalStatuses;
    }

    public ApprovalStatus getApprovalStatusById(Long id) {
        return approvalStatusMap.get(id);
    }

    public List<LedgerCategory> getLedgerCategories() {
        return cachedLedgerCategories;
    }

    public LedgerCategory getLedgerCategoryById(Long id) {
        return cachedLedgerCategoryMap.get(id);
    }

    public List<PaymentMode> getPaymentModes() {
        return cachedPaymentModes;
    }
    public PaymentMode getPaymentModeById(Long id) {
        return cachedPaymentModeMap.get(id);
    }
}
