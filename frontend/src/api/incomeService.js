import api from "./axiosConfig";

export const incomeService = {
  // Submits the drafted income record to the Spring Boot backend
  createIncomeDraft: async (apartmentId, payload) => {
    // Note: Check your Spring Boot IncomeController to ensure this URL matches your @PostMapping exactly.
    const response = await api.post(
      `/apartments/${apartmentId}/incomes/drafts`,
      payload,
    );
    return response.data;
  },

  updateIncomeDraft: async (apartmentId, draftId, payload) => {
    const response = await api.put(
      `/apartments/${apartmentId}/incomes/drafts/${draftId}`,
      payload,
    );
    return response.data;
  },

  getPendingDrafts: async (apartmentId, year, month) => {
    const response = await api.get(
      `/apartments/${apartmentId}/incomes/drafts`,
      {
        params: {
          approvalStatus: "PENDING",
          year: year,
          month: month,
        },
      },
    );
    return response.data;
  },

  getAllDrafts: async (apartmentId, year, month) => {
    const response = await api.get(
      `/apartments/${apartmentId}/incomes/drafts`,
      {
        params: {
          year: year,
          month: month,
        },
      },
    );
    return response.data;
  },

  approveDrafts: async (apartmentId, draftIds) => {
    const response = await api.post(
      `/apartments/${apartmentId}/incomes/drafts/approve-bulk`,
      {
        draftIds: draftIds,
      },
    );
    return response.data;
  },

  rejectDrafts: async (apartmentId, draftIds, rejectReason) => {
    const response = await api.post(
      `/apartments/${apartmentId}/incomes/drafts/reject-bulk`,
      {
        draftIds: draftIds,
        comments: rejectReason,
      },
    );
    return response.data;
  },
};
