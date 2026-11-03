import axios from "axios";

const api = axios.create({
  // Adjust the port if your Spring Boot runs on 8081 or something else
  //baseURL: "http://localhost:8081/al/api/v1",
  baseURL: "/al/api/v1",
  headers: {
    "Content-Type": "application/json",
  },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem("jwt_token");

  if (token) {
    config._authToken = token;
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const requestToken = error.config?._authToken;
    const currentToken = localStorage.getItem("jwt_token");
    const isLoginOrRegistration = /\/auth\/(login|register)(?:\?|$)/.test(
      error.config?.url ?? "",
    );

    if (
      error.response?.status === 401 &&
      requestToken &&
      requestToken === currentToken &&
      !isLoginOrRegistration
    ) {
      window.dispatchEvent(new Event("auth:session-expired"));
    }

    return Promise.reject(error);
  },
);

export default api;
