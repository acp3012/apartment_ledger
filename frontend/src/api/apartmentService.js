import api from "./axiosConfig";

// Initialize as empty objects, NOT null!
let flatsCache = {};
let apartmentCache = {};

export const apartmentService = {
  getFlatsByApartment: async (apartmentId) => {
    if (!flatsCache[apartmentId]) {
      const response = await api.get(`/apartments/${apartmentId}/flats`);
      flatsCache[apartmentId] = response.data;
    }
    return flatsCache[apartmentId];
  },

  getApartmentDetails: async (apartmentId) => {
    if (!apartmentCache[apartmentId]) {
      const response = await api.get(`/apartments/${apartmentId}`);
      apartmentCache[apartmentId] = response.data;
    }
    return apartmentCache[apartmentId];
  },

  createFlat: async (apartmentId, flatData) => {
    const response = await api.post(
      `/apartments/${apartmentId}/flats`,
      flatData,
    );
    delete flatsCache[apartmentId];
    return response.data;
  },

  updateFlat: async (apartmentId, flatId, flatData) => {
    const response = await api.put(
      `/apartments/${apartmentId}/flats/${flatId}`,
      flatData,
    );
    delete flatsCache[apartmentId];
    return response.data;
  },

  updateGoLivePeriod: async (apartmentId, period) => {
    const response = await api.patch(
      `/apartments/${apartmentId}/go-live-period`,
      period,
    );
    apartmentCache[apartmentId] = response.data;
    return response.data;
  },

  completeInitialSetup: async (apartmentId) => {
    const response = await api.patch(
      `/apartments/${apartmentId}/setup-completed`,
    );
    return response.data;
  },

  getApplicableRate: async (apartmentId, flatId, transactionDate) => {
    const response = await api.get(
      `/apartments/${apartmentId}/maintenance-rates/current`,
      {
        params: { flatId, date: transactionDate },
      },
    );
    return response.data;
  },
};
