import { useParams, useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import IncomeTab from "../components/IncomeTab";
import ApproveIncomePage from "./ApproveIncomePage";
import LedgerPeriodBanner from "../components/LedgerPeriodBanner";

const IncomePage = () => {
  const { apartmentId: routeParamId } = useParams();
  const { user } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const apartmentId = Number(routeParamId ?? user?.apartmentId ?? 0);
  const isApprovalView = location.pathname.includes("approve");

  return (
    <div>
      {/* Header & Sub-Tabs */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between mb-6 gap-4">
        <div>
          <h2 className="text-2xl font-bold text-slate-900">
            Income Management
          </h2>
          <p className="text-slate-600 mt-1">
            {isApprovalView
              ? "Review and authorize pending income drafts."
              : "Record new community income entries."}
          </p>
        </div>

        {/* Pill Sub-Tabs */}
        <div className="flex bg-slate-200/70 p-1 rounded-xl border border-slate-200">
          <button
            onClick={() => navigate(`/apartments/${apartmentId}/incomes`)}
            className={`px-4 py-2 rounded-lg text-xs font-semibold transition-all ${
              !isApprovalView
                ? "bg-white text-blue-600 shadow-sm"
                : "text-slate-600 hover:text-slate-900"
            }`}>
            Record Entry
          </button>
          <button
            onClick={() =>
              navigate(`/apartments/${apartmentId}/approve-incomes`)
            }
            className={`px-4 py-2 rounded-lg text-xs font-semibold transition-all ${
              isApprovalView
                ? "bg-white text-blue-600 shadow-sm"
                : "text-slate-600 hover:text-slate-900"
            }`}>
            Approvals Queue
          </button>
        </div>
      </div>
      <LedgerPeriodBanner apartmentId={apartmentId} />
      {/* Content Card Container */}
      <div className="bg-white p-6 rounded-2xl shadow-sm border border-slate-200">
        {isApprovalView ? (
          <ApproveIncomePage apartmentId={apartmentId} />
        ) : (
          <IncomeTab
            apartmentId={apartmentId}
            makerId={user?.id ?? user?.userId}
          />
        )}
      </div>
    </div>
  );
};

export default IncomePage;
