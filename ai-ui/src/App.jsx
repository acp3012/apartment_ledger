import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider, useAuth } from "./context/AuthContext";
import AuthPage      from "./pages/auth/AuthPage";
import DashboardPage from "./pages/dashboard/DashboardPage";

function ProtectedRoute({ children }) {
  const { session } = useAuth();
  return session ? children : <Navigate to="/" replace />;
}

function AppRoutes() {
  const { session } = useAuth();
  return (
    <Routes>
      {/* If already logged in, / redirects to dashboard */}
      <Route path="/"
        element={session ? <Navigate to="/dashboard" replace /> : <AuthPage />}
      />
      <Route path="/dashboard"
        element={
          <ProtectedRoute>
            <DashboardPage />
          </ProtectedRoute>
        }
      />
      {/* Catch-all */}
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <AppRoutes />
      </AuthProvider>
    </BrowserRouter>
  );
}
