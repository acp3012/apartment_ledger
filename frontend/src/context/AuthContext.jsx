/* eslint-disable react-refresh/only-export-components */
import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useRef,
  useState,
} from "react";
import { toast } from "react-toastify";

const AuthContext = createContext(null);

const getTokenExpiration = (token) => {
  try {
    const payload = token?.split(".")[1];
    if (!payload) return null;

    const normalizedPayload = payload.replace(/-/g, "+").replace(/_/g, "/");
    const paddedPayload = normalizedPayload.padEnd(
      Math.ceil(normalizedPayload.length / 4) * 4,
      "=",
    );
    const claims = JSON.parse(atob(paddedPayload));
    return Number.isFinite(claims.exp) ? claims.exp * 1000 : null;
  } catch {
    return null;
  }
};

const clearStoredAuth = () => {
  localStorage.removeItem("app_user");
  localStorage.removeItem("jwt_token");
};

const normalizeUser = (userData) => {
  if (!userData) return null;

  return {
    ...userData,
    id: userData.id ?? userData.userId ?? null,
    apartmentId:
      userData.apartmentId ??
      userData.apartment_id ??
      userData.apartment?.id ??
      null,
  };
};

const getStoredUser = () => {
  const token = localStorage.getItem("jwt_token");
  const storedUser = localStorage.getItem("app_user");
  const expiration = getTokenExpiration(token);

  if (!token || !storedUser || !expiration || expiration <= Date.now()) {
    clearStoredAuth();
    return null;
  }

  try {
    return normalizeUser(JSON.parse(storedUser));
  } catch {
    clearStoredAuth();
    return null;
  }
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(getStoredUser);
  const sessionExpiredRef = useRef(false);

  const login = (loginResponse) => {
    const { token, ...userData } = loginResponse ?? {};
    if (!token) throw new Error("Login response did not include a token.");

    const normalizedUser = normalizeUser(userData);
    sessionExpiredRef.current = false;
    setUser(normalizedUser);
    localStorage.setItem("jwt_token", token);
    localStorage.setItem("app_user", JSON.stringify(normalizedUser));
  };

  const updateUser = useCallback(
    (updates) => {
      if (!user) return;
      const updatedUser = { ...user, ...updates };
      setUser(updatedUser);
      localStorage.setItem("app_user", JSON.stringify(updatedUser));
    },
    [user],
  );

  const logout = useCallback(() => {
    setUser(null);
    clearStoredAuth();
  }, []);

  const handleSessionExpired = useCallback(() => {
    if (!user || sessionExpiredRef.current) return;

    sessionExpiredRef.current = true;
    toast.info("Your session has expired. Please log in again.");
    logout();
  }, [logout, user]);

  useEffect(() => {
    window.addEventListener("auth:session-expired", handleSessionExpired);
    return () =>
      window.removeEventListener("auth:session-expired", handleSessionExpired);
  }, [handleSessionExpired]);

  useEffect(() => {
    if (!user) return undefined;

    const expiration = getTokenExpiration(localStorage.getItem("jwt_token"));
    const timer = window.setTimeout(
      handleSessionExpired,
      expiration ? Math.max(0, expiration - Date.now()) : 0,
    );

    return () => window.clearTimeout(timer);
  }, [handleSessionExpired, user]);

  return (
    <AuthContext.Provider value={{ user, login, logout, updateUser }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider");
  }

  return context;
};

export default AuthProvider;
