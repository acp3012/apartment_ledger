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
};
