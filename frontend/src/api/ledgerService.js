import api from "./axiosConfig";

export const ledgerService = {
  // Fetch the summary preview for the top KPI cards
  getLedgerSummary: async (apartmentId, year, month) => {
    const response = await api.get(`/apartments/${apartmentId}/ledger/summary`, {
      params: { year, month },
    });
    return response.data;
  },

  // Fetch the detailed list of transactions for the table
  getLedgerDetails: async (apartmentId, year, month) => {
    const response = await api.get(`/apartments/${apartmentId}/ledger/details`, {
      params: { year, month },
    });
    return response.data;
  },
  getPaymentModeSummary: async (apartmentId, year, month) => {
    const response = await api.get(`/apartments/${apartmentId}/incomes/summary/payment-mode`, {
      params: { year, month },
    });
    return response.data;
  },
  getExpenseCategorySummary: async (apartmentId, year, month) => {
    const response = await api.get(`/apartments/${apartmentId}/expenses/summary/ledger-category`, {
      params: { year, month },
    });
    return response.data;
  },
  // New endpoint for flat payment status
  getFlatPaymentStatus: async (apartmentId, flatId, year, month) => {
    const response = await api.get(`/apartments/${apartmentId}/flats/${flatId}/payment-status`, {
      params: { year, month },
    });
    return response.data;
  },
   getActiveLedgerPeriod: async (apartmentId) => {
        // Assumes your backend returns something like: { month: "September", year: 2026 }
        const response = await api.get(`/apartments/${apartmentId}/ledger/active-period`);
        return response.data;
    },
  
  // Close the active ledger period
  closeActivePeriod: async (apartmentId, year, month) => {
    const response = await api.post(`/apartments/${apartmentId}/ledger/close`, null, {
      params: { apartmentId, year, month }
    });
    return response.data;
  }
};
