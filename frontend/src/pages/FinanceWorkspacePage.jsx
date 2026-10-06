import { useState } from "react";
import { useParams } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import IncomeTab from "../components/IncomeTab";
import ExpenseMakerForm from "./ExpenseMakerForm";
import ApproveIncomePage from "./ApproveIncomePage";
import CheckerInbox from "./CheckerInbox";

const FinanceWorkspacePage = ({
  defaultType = "income",
  defaultMode = "entry",
}) => {
  const { apartmentId: routeParamId } = useParams();
  const { user } = useAuth();

  const apartmentId = Number(
    routeParamId ?? user?.apartmentId ?? user?.apartment_id ?? 0,
  );
  const makerId = Number(user?.id ?? user?.userId ?? 0);

  // State for toggles
  const [transactionType, setTransactionType] = useState(defaultType); // "income" or "expense"
  const [mode, setMode] = useState(defaultMode); // "entry" (maker) or "approval" (checker)

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
      {/* Top Header & Interactive Toggles */}
      <div className="flex flex-col md:flex-row justify-between items-start md:items-center bg-white p-5 rounded-2xl shadow-sm border border-slate-200 mb-6 gap-4">
        <div>
          <h2 className="text-2xl font-bold text-slate-900 capitalize">
            {transactionType} Management
          </h2>
          <p className="text-sm text-slate-500 mt-0.5">
            {mode === "entry"
              ? "Record and submit drafts for review"
              : "Review and approve pending drafts"}
          </p>
        </div>

        {/* Toggle Switch Group */}
        <div className="flex flex-wrap items-center gap-3 w-full md:w-auto">
          {/* Income vs Expense Toggle */}
          <div className="flex bg-slate-100 p-1 rounded-xl border border-slate-200">
            <button
              onClick={() => setTransactionType("income")}
              className={`flex-1 md:flex-none px-4 py-2 rounded-lg text-sm font-semibold transition-all ${
                transactionType === "income"
                  ? "bg-white text-blue-600 shadow-sm"
                  : "text-slate-600 hover:text-slate-900"
              }`}>
              Income
            </button>
            <button
              onClick={() => setTransactionType("expense")}
              className={`flex-1 md:flex-none px-4 py-2 rounded-lg text-sm font-semibold transition-all ${
                transactionType === "expense"
                  ? "bg-white text-blue-600 shadow-sm"
                  : "text-slate-600 hover:text-slate-900"
              }`}>
              Expenses
            </button>
          </div>

          {/* Maker (Entry) vs Checker (Approval) Toggle - Admin Only or general */}
          {user?.isAdmin && (
            <div className="flex bg-slate-100 p-1 rounded-xl border border-slate-200">
              <button
                onClick={() => setMode("entry")}
                className={`flex-1 md:flex-none px-3 py-2 rounded-lg text-xs font-semibold transition-all ${
                  mode === "entry"
                    ? "bg-slate-900 text-white shadow-sm"
                    : "text-slate-600 hover:text-slate-900"
                }`}>
                Record / Drafts
              </button>
              <button
                onClick={() => setMode("approval")}
                className={`flex-1 md:flex-none px-3 py-2 rounded-lg text-xs font-semibold transition-all ${
                  mode === "approval"
                    ? "bg-slate-900 text-white shadow-sm"
                    : "text-slate-600 hover:text-slate-900"
                }`}>
                Approvals
              </button>
            </div>
          )}
        </div>
      </div>

      {/* Dynamic Workspace Container */}
      <div className="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden">
        {transactionType === "income" ? (
          mode === "entry" ? (
            <IncomeTab
              apartmentId={apartmentId}
              makerId={makerId}
            />
          ) : (
            <ApproveIncomePage apartmentId={apartmentId} />
          )
        ) : mode === "entry" ? (
          <ExpenseMakerForm
            apartmentId={apartmentId}
            makerId={makerId}
          />
        ) : (
          <CheckerInbox apartmentId={apartmentId} />
        )}
      </div>
    </div>
  );
};

export default FinanceWorkspacePage;
