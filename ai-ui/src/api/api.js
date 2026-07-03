// ─────────────────────────────────────────────────────────────────
// 🔧 Change this to your Spring Boot server URL when testing
// ─────────────────────────────────────────────────────────────────
const BASE_URL = "/api/v1";   // proxied to http://localhost:8080 via vite.config.js

async function http(path, options = {}) {
  const token = localStorage.getItem("al_token");
  const res = await fetch(`${BASE_URL}${path}`, {
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    ...options,
  });

  if (res.status === 204) return null;

  const data = await res.json().catch(() => ({}));
  if (!res.ok) throw new Error(data.message || `Error ${res.status}`);
  return data;
}

// ── Auth ──────────────────────────────────────────────────────────
export const authApi = {
  /**
   * POST /api/v1/auth/login
   * Body:    { email, password }
   * Returns: { token, userId, userName, email, role, flatId, apartmentName }
   */
  login: (body) =>
    http("/auth/login", { method: "POST", body: JSON.stringify(body) }),
};

// ── Users ─────────────────────────────────────────────────────────
export const userApi = {
  /**
   * POST /api/v1/users  (register)
   * Body matches AppUserRequest DTO:
   *   { userName, email, mobileNumber, password, roleId, flatId? }
   * Returns: AppUserResponse
   */
  register: (body) =>
    http("/users", { method: "POST", body: JSON.stringify(body) }),

  getById: (id) => http(`/users/${id}`),
};

// ── Flats (for register dropdown — future use) ────────────────────
export const flatApi = {
  /** GET /api/v1/flats/apartment/{apartmentId} */
  listByApartment: (apartmentId) =>
    http(`/flats/apartment/${apartmentId}`),
};
