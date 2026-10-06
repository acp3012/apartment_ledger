import { useEffect } from "react";
import { useNavigate, Routes, Route, useLocation } from "react-router-dom";
import ExpenseMakerForm from "./ExpenseMakerForm";
import CheckerInbox from "./CheckerInbox";
import { useAuth } from "../context/AuthContext";

const DashboardOverview = ({ user, navigate }) => (
  <>
    <header className="mb-8">
      <div className="flex flex-col gap-3 lg:flex-row lg:items-end lg:justify-between">
        <div>
          <p className="text-xs font-bold uppercase tracking-wider text-blue-600 mb-1">
            Overview
          </p>
          <h1 className="text-3xl font-bold tracking-tight text-slate-900 sm:text-4xl">
            Welcome, {user.displayName}
          </h1>
        </div>
        <div className="inline-flex items-center rounded-full border border-blue-200 bg-blue-50 px-3.5 py-1.5 text-sm font-medium text-blue-700 shadow-xs">
          Flat {user.flatNumber}
        </div>
      </div>
    </header>

    <div className="grid grid-cols-1 gap-6 md:grid-cols-3">
      <div className="p-6 bg-white rounded-2xl border border-slate-200 shadow-sm">
        <div className="mb-4 flex items-center justify-between">
          <h3 className="text-sm font-medium text-slate-500">
            Current Maintenance
          </h3>
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-emerald-50 text-lg text-emerald-600 font-bold">
            ₹
          </div>
        </div>
        <p className="text-3xl font-bold tracking-tight text-slate-900">
          ₹0.00
        </p>
        <p className="mt-3 inline-flex items-center rounded-full bg-emerald-50 px-2.5 py-1 text-xs font-medium text-emerald-700">
          Up to date
        </p>
      </div>

      <button
        type="button"
        onClick={() => navigate("/monthly-statement")}
        className="flex flex-col justify-between p-6 text-left transition-transform duration-200 hover:-translate-y-0.5 bg-white rounded-2xl border border-slate-200 shadow-sm hover:shadow-md">
        <div className="mb-4 flex items-center justify-between">
          <h3 className="text-sm font-medium text-slate-500">
            Monthly Statement
          </h3>
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-indigo-50 text-lg text-indigo-600">
            ↗
          </div>
        </div>
        <div>
          <p className="text-3xl font-bold tracking-tight text-slate-900">
            ₹48.6k
          </p>
          <p className="mt-3 text-sm text-slate-500">
            View income, expense and closing balance
          </p>
        </div>
      </button>

      {user.isAdmin && (
        <div className="border-blue-200 bg-gradient-to-r from-blue-50 to-indigo-50 p-6 md:col-span-1 rounded-2xl border shadow-sm">
          <div className="flex items-start justify-between gap-4">
            <div>
              <h3 className="text-lg font-semibold text-blue-900">
                Admin Mode Active
              </h3>
              <p className="mt-2 text-sm text-blue-700">
                You have full permission to record and approve community income
                and expenses.
              </p>
            </div>
            <div className="flex h-11 w-11 items-center justify-center rounded-xl bg-blue-600 text-lg text-white shadow-md">
              ✓
            </div>
          </div>
        </div>
      )}
    </div>
  </>
);

/* Expense Workspace Page with Sub-Tabs for Record & Approve */
const ExpenseWorkspaceWrapper = ({ user, navigate, location }) => {
  const isApproval = location.pathname.includes("approve");

  return (
    <div>
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between mb-6 gap-4">
        <div>
          <h2 className="text-2xl font-bold text-slate-900">Expense/Payment</h2>
          <p className="text-slate-600 mt-1">
            {isApproval
              ? "Review and approve pending community expenses."
              : "Add expense records for the active ledger period."}
          </p>
        </div>

        {/* Pill Sub-Tabs */}
        <div className="flex bg-slate-200/70 p-1 rounded-xl border border-slate-200">
          <button
            onClick={() => navigate("/dashboard/expense/draft")}
            className={`px-4 py-2 rounded-lg text-xs font-semibold transition-all ${
              !isApproval
                ? "bg-white text-blue-600 shadow-sm"
                : "text-slate-600 hover:text-slate-900"
            }`}>
            Record Expense
          </button>
          <button
            onClick={() => navigate("/dashboard/expense/approve")}
            className={`px-4 py-2 rounded-lg text-xs font-semibold transition-all ${
              isApproval
                ? "bg-white text-blue-600 shadow-sm"
                : "text-slate-600 hover:text-slate-900"
            }`}>
            Approve Expenses
          </button>
        </div>
      </div>

      <div className="bg-white p-6 rounded-2xl shadow-sm border border-slate-200">
        {isApproval ? (
          <CheckerInbox apartmentId={user.apartmentId} />
        ) : (
          <ExpenseMakerForm
            apartmentId={user.apartmentId}
            makerId={user.id}
          />
        )}
      </div>
    </div>
  );
};

const DashboardPage = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { user } = useAuth();

  useEffect(() => {
    if (!user) {
      navigate("/login");
    }
  }, [navigate, user]);

  if (!user)
    return <div className="p-8 text-center text-slate-500">Loading...</div>;

  return (
    <Routes>
      <Route
        path="/"
        element={
          <DashboardOverview
            user={user}
            navigate={navigate}
          />
        }
      />
      <Route
        path="expense/*"
        element={
          <ExpenseWorkspaceWrapper
            user={user}
            navigate={navigate}
            location={location}
          />
        }
      />
    </Routes>
  );
};

export default DashboardPage;
