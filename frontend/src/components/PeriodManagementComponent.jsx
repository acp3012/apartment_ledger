import React, { useState } from "react";
import { ledgerService } from "../api/ledgerService";
import { usePeriod } from "../context/PeriodContext";
import { useAuth } from "../context/AuthContext";

const PeriodManagementComponent = () => {
  const { activePeriod, refreshPeriod } = usePeriod();
  const { user } = useAuth();
  const apartmentId = user?.apartmentId || 1;

  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");
  const [errorMessage, setErrorMessage] = useState("");

  const handleClosePeriod = async () => {
    try {
      setLoading(true);
      setMessage("");
      setErrorMessage("");

      // 1. Call your backend endpoint to close the period (adjust method name as per your ledgerService)
      await ledgerService.closeActivePeriod(apartmentId, activePeriod.year, activePeriod.month);

      // 2. Instantly update all pages across the app!
      await refreshPeriod();

      setMessage(`Successfully closed ${activePeriod.month}/${activePeriod.year} and advanced period.`);
    } catch (error) {
      console.error("Failed to close period:", error);
      setErrorMessage("Failed to close the active period. Check active transactions.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-6 space-y-4">
      <div>
        <p className="text-xs font-semibold uppercase tracking-wider text-slate-400">Ledger Administration</p>
        <h3 className="text-lg font-bold text-slate-800">
          Active Period: {new Date(0, activePeriod.month - 1).toLocaleString("en", { month: "long" })} {activePeriod.year}
        </h3>
      </div>

      <p className="text-xs text-slate-600 leading-relaxed">
        Closing this accounting period will finalize transactions for this month and roll over the active books to the next period.
      </p>

      {message && <div className="p-3 bg-emerald-50 text-emerald-700 text-xs font-medium rounded-xl">{message}</div>}
      {errorMessage && <div className="p-3 bg-red-50 text-red-700 text-xs font-medium rounded-xl">{errorMessage}</div>}

      <button
        onClick={handleClosePeriod}
        disabled={loading}
        className="bg-red-600 hover:bg-red-700 text-white px-5 py-2.5 rounded-xl text-sm font-bold shadow-sm transition-all disabled:opacity-50">
        {loading ? "Processing Roll-over..." : "Close & Advance Period"}
      </button>
    </div>
  );
};

export default PeriodManagementComponent;