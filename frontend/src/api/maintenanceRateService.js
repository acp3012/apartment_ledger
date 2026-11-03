import api from "./axiosConfig";

export const maintenanceRateService = {
  getApartmentRates: async (apartmentId) => {
    const response = await api.get(
      `/apartments/${apartmentId}/maintenance-rates`,
    );
    return response.data;
  },

  createApartmentRate: async (apartmentId, rate) => {
    const response = await api.post(
      `/apartments/${apartmentId}/maintenance-rates`,
      rate,
    );
    return response.data;
  },
};
