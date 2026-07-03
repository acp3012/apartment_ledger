import { createContext, useContext, useState, useCallback } from "react";

const AuthContext = createContext(null);

/**
 * Stores session data in localStorage so the user stays logged in
 * after a page refresh.
 *
 * Shape of session:
 * {
 *   token, userId, userName, email,
 *   role,            // "ADMIN" | "OWNER"
 *   flatId,          // null for ADMIN
 *   apartmentName,   // null for ADMIN
 *   loginTime        // ISO string
 * }
 */
function loadSession() {
  try {
    const raw = localStorage.getItem("al_session");
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

export function AuthProvider({ children }) {
  const [session, setSession] = useState(loadSession);

  const login = useCallback((data) => {
    const sess = { ...data, loginTime: new Date().toISOString() };
    localStorage.setItem("al_session", JSON.stringify(sess));
    setSession(sess);
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem("al_session");
    setSession(null);
  }, []);

  return (
    <AuthContext.Provider value={{ session, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
