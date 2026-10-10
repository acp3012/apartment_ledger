import { useState } from "react";
import { ledgerService } from "../api/ledgerService";
import { usePeriod } from "../context/PeriodContext";
import { useAuth } from "../context/AuthContext";
import ConfirmationModal from "./ConfirmationModal";

const PeriodManagementComponent = () => {
  const { activePeriod, refreshPeriod } = usePeriod();
  const { user } = useAuth();
  const apartmentId = user?.apartmentId || 1;

  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");
  const [errorMessage, setErrorMessage] = useState("");
  const [isConfirmOpen, setIsConfirmOpen] = useState(false);

  const handleClosePeriod = async () => {
    try {
      setLoading(true);
      setMessage("");
      setErrorMessage("");

      // Calls your backend endpoint with current active year & month
      await ledgerService.closeActivePeriod(
        apartmentId,
        activePeriod.year,
        activePeriod.month,
      );

      // Instantly refresh the global context so all screens update to the new month
      await refreshPeriod();

      setMessage(
        `Successfully closed ${activePeriod.month}/${activePeriod.year} and advanced the active period!`,
      );
      setIsConfirmOpen(false);
    } catch (error) {
      console.error("Failed to close period:", error);
      // Handles 400 Bad Request or draft entry validation failures gracefully
      const errorMsg =
        error.response?.data?.message ||
        "Failed to close period. Ensure all draft entries are approved or rejected.";
      setErrorMessage(errorMsg);
    } finally {
      setLoading(false);
    }
  };

  if (!activePeriod) {
    return null;
  }

  const monthName = activePeriod.month
    ? new Date(0, activePeriod.month - 1).toLocaleString("en", {
        month: "long",
      })
    : "";

  return (
    <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-6 space-y-4">
      <div>
        <p className="text-xs font-semibold uppercase tracking-wider text-slate-400">
          Ledger Administration
        </p>
        <h3 className="text-lg font-bold text-slate-800">
          Active Period: {monthName} {activePeriod.year}
        </h3>
      </div>

      <p className="text-xs text-slate-600 leading-relaxed">
        Closing this period requires all draft incomes and expenses to be
        reviewed (approved or rejected). Once closed, monthly totals are
        archived and the system rolls over to the next month.
      </p>

      {message && (
        <div className="p-3 bg-emerald-50 border border-emerald-200 text-emerald-800 text-xs font-medium rounded-xl">
          {message}
        </div>
      )}

      {errorMessage && (
        <div className="p-3 bg-rose-50 border border-rose-200 text-rose-800 text-xs font-medium rounded-xl">
          {errorMessage}
        </div>
      )}

      <button
        onClick={() => {
          setMessage("");
          setErrorMessage("");
          setIsConfirmOpen(true);
        }}
        disabled={loading}
        className="bg-indigo-600 hover:bg-indigo-700 text-white px-5 py-2.5 rounded-xl text-sm font-bold shadow-sm transition-all disabled:opacity-50">
        {loading ? "Validating & Closing..." : "Close & Advance Period"}
      </button>

      <ConfirmationModal
        isOpen={isConfirmOpen}
        title="Confirm Period Close"
        variant="primary"
        errorMessage={errorMessage}
        confirmLabel="Confirm Close & Advance"
        loadingLabel="Closing..."
        isLoading={loading}
        onCancel={() => {
          setIsConfirmOpen(false);
          setErrorMessage("");
        }}
        onConfirm={handleClosePeriod}>
        <p className="text-sm leading-relaxed text-slate-600">
          You are about to close{" "}
          <strong className="text-slate-900">
            {monthName} {activePeriod.year}
          </strong>
          . All income and expense drafts must be approved or rejected. The
          ledger will then advance to the next month. Continue?
        </p>
      </ConfirmationModal>
    </div>
  );
};

export default PeriodManagementComponent;
