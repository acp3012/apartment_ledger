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

  getApplicableRate: async (apartmentId, flatId, transactionDate) => {
    // (Your existing code here is perfect)
    const response = await api.get(
      `/apartments/${apartmentId}/maintenance-rates/current`,
      {
        params: { flatId, date: transactionDate },
      },
    );
    return response.data;
  },
};
