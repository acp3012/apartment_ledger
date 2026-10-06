import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import MainLayout from "./layouts/MainLayout";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import DashboardPage from "./pages/DashboardPage";
import FinanceWorkspacePage from "./pages/FinanceWorkspacePage";
import IncomePage from "./pages/IncomePage";
import MonthlyStatementPage from "./pages/MonthlyStatementPage";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* PUBLIC ROUTES */}
        <Route
          path="/login"
          element={<LoginPage />}
        />
        <Route
          path="/register"
          element={<RegisterPage />}
        />

        {/* SECURE ROUTES WRAPPED IN SIDEBAR LAYOUT */}
        <Route element={<MainLayout />}>
          <Route
            path="/"
            element={
              <Navigate
                to="/dashboard"
                replace
              />
            }
          />

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
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;
