import api from "./axiosConfig";
let paymentModeCache = null;
let expenseCategoriesCache = null;
let incomeCategoriesCache = null;

export const masterDataService = {
  getExpenseCategories: async () => {
    if(!expenseCategoriesCache){
	    const response = await api.get("/master/ledger-categories/DR");
    	expenseCategoriesCache = response.data;
    }
    return expenseCategoriesCache;
  },
  
  getIncomeCategories: async () => {
    if(!incomeCategoriesCache) {
      
    const response = await api.get("/master/ledger-categories/CR");
    incomeCategoriesCache = response.data;
    }
    return incomeCategoriesCache;
  },

  getPaymentModes: async () => {
    if(!paymentModeCache) {
    	const response = await api.get("/master/payment-modes");
    	paymentModeCache = response.data;
    }
    return paymentModeCache;
  },
 
};
