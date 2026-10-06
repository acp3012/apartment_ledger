// src/api/expenseService.js
import api from "./axiosConfig";

export const expenseService = {
  submitExpenseDraft: async (apartmentId, draftRequest) => {
    const response = await api.post(
      `/apartments/${apartmentId}/expenses/drafts`,
      draftRequest,
      { params: { apartmentId } },
    );
    return response.data;
  },

  updateExpenseDraft: async (apartmentId, draftId, draftRequest) => {
    const response = await api.put(
      `/apartments/${apartmentId}/expenses/drafts/${draftId}`,
      draftRequest,
    );
    return response.data;
  },

  getPendingDraftsForPeriod: async (apartmentId, year, month) => {
    const response = await api.get(
      `/apartments/${apartmentId}/expenses/drafts`,
      { params: { approvalStatus: "pending", year, month } },
    );
    return response.data;
  },

  getPendingDrafts: async (apartmentId) => {
    const response = await api.get(
      `/apartments/${apartmentId}/expenses/drafts`,
      { params: { approvalStatus: "pending" } },
    );
    return response.data;
  },

  // Updated bulk approval matching your curl command
  approveDrafts: async (apartmentId, draftIds, userId) => {
    const response = await api.post(
      `/apartments/${apartmentId}/expenses/drafts/approve-bulk`,
      {
        userId: Number(userId),
        draftIds: draftIds,
      }
    );
    return response.data;
  },

rejectDrafts: async (apartmentId, draftIds, userId, rejectReason) => {
    const response = await api.post(
      `/apartments/${apartmentId}/expenses/drafts/reject-bulk`,
      {
        userId: Number(userId),
        draftIds: draftIds,
        rejectReason: rejectReason || "",
      }
    );
    return response.data;
  },
};
