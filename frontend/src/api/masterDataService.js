import api from "./axiosConfig";
let paymentModeCache = null;
let expenseCategoriesCache = null;
let incomeCategoriesCache = null;

export const masterDataService = {
  getExpenseCategories: async () => {
    if (
      !Array.isArray(expenseCategoriesCache) ||
      expenseCategoriesCache.length === 0
    ) {
      const response = await api.get("/master/ledger-categories/DR");
      expenseCategoriesCache = Array.isArray(response.data)
        ? response.data
        : [];
    }
    return expenseCategoriesCache;
  },

  getIncomeCategories: async () => {
    if (
      !Array.isArray(incomeCategoriesCache) ||
      incomeCategoriesCache.length === 0
    ) {
      const response = await api.get("/master/ledger-categories/CR");
      incomeCategoriesCache = Array.isArray(response.data) ? response.data : [];
    }
    return incomeCategoriesCache;
  },

  getPaymentModes: async () => {
    if (!Array.isArray(paymentModeCache) || paymentModeCache.length === 0) {
      const response = await api.get("/master/payment-modes");
      paymentModeCache = Array.isArray(response.data) ? response.data : [];
    }
    return paymentModeCache;
  },
};
