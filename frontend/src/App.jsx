import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import MainLayout from "./layouts/MainLayout";
import LoginPage from "./pages/LoginPage";
import FirstTimeAdminPage from "./pages/FirstTimeAdminPage";
import FirstTimeAdminPasswordPage from "./pages/FirstTimeAdminPasswordPage";
import RegisterPage from "./pages/RegisterPage";
import DashboardPage from "./pages/DashboardPage";
import FinanceWorkspacePage from "./pages/FinanceWorkspacePage";
import IncomePage from "./pages/IncomePage";
import MonthlyStatementPage from "./pages/MonthlyStatementPage";
import MasterEntriesPage from "./pages/MasterEntriesPage";
import OpeningBalancePage from "./pages/OpeningBalancePage";
import InitialSetupPage from "./pages/InitialSetupPage";
import FlatSetupPage from "./pages/FlatSetupPage";
import GoLivePeriodSetupPage from "./pages/GoLivePeriodSetupPage";
import MaintenanceFeeSetupPage from "./pages/MaintenanceFeeSetupPage";
import { useAuth } from "./context/AuthContext";

const RootRoute = () => {
  const { user } = useAuth();
  return (
    <Navigate
      to={user ? "/dashboard" : "/login"}
      replace
    />
  );
};

const PublicOnlyRoute = ({ element }) => {
  const { user } = useAuth();
  return user ? (
    <Navigate
      to="/dashboard"
      replace
    />
  ) : (
    element
  );
};

function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* PUBLIC ROUTES */}
        <Route
          path="/"
          element={<RootRoute />}
        />
        <Route
          path="/login"
          element={<PublicOnlyRoute element={<LoginPage />} />}
        />
        <Route
          path="/first-time-admin"
          element={<PublicOnlyRoute element={<FirstTimeAdminPage />} />}
        />
        <Route
          path="/first-time-admin/password"
          element={
            <PublicOnlyRoute element={<FirstTimeAdminPasswordPage />} />
          }
        />
        <Route
          path="/register"
          element={<PublicOnlyRoute element={<RegisterPage />} />}
        />

        {/* SECURE ROUTES WRAPPED IN SIDEBAR LAYOUT */}
        <Route element={<MainLayout />}>
          <Route
            path="/dashboard"
            element={<DashboardPage />}
          />
          <Route
            path="/dashboard/expense"
            element={
              <FinanceWorkspacePage
                defaultType="expense"
                defaultMode="entry"
              />
            }
          />
          <Route
            path="/dashboard/expense/draft"
            element={
              <FinanceWorkspacePage
                defaultType="expense"
                defaultMode="entry"
              />
            }
          />
          <Route
            path="/apartments/:apartmentId/incomes"
            element={<IncomePage />}
          />
          <Route
            path="/apartments/:apartmentId/approve-incomes"
            element={<IncomePage />}
          />
          <Route
            path="/monthly-statement"
            element={<MonthlyStatementPage />}
          />
          <Route
            path="/admin/master-entries"
            element={<MasterEntriesPage />}
          />
          <Route
            path="/admin/setup"
            element={<InitialSetupPage />}
          />
          <Route
            path="/admin/setup/flats"
            element={<FlatSetupPage />}
          />
          <Route
            path="/admin/setup/period"
            element={<GoLivePeriodSetupPage />}
          />
          <Route
            path="/admin/maintenance-fees"
            element={<MaintenanceFeeSetupPage />}
          />
          <Route
            path="/admin/opening-balance"
            element={<OpeningBalancePage />}
          />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;
