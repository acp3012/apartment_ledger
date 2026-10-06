/* eslint-disable react-refresh/only-export-components */
import { createContext, useState, useContext } from "react";

const AuthContext = createContext(null);

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

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(() => {
    const storedUser = localStorage.getItem("app_user");
    return storedUser ? normalizeUser(JSON.parse(storedUser)) : null;
  });

  const login = (userData) => {
    const normalizedUser = normalizeUser(userData);
    setUser(normalizedUser);
    localStorage.setItem("app_user", JSON.stringify(normalizedUser));
  };

  const logout = () => {
    setUser(null);
    localStorage.removeItem("app_user");
    localStorage.removeItem("jwt_token");
  };

  return (
    <AuthContext.Provider value={{ user, login, logout }}>
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
