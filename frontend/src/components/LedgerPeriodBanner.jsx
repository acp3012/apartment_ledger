import React, { useState, useEffect } from "react";
import { masterDataService } from "../api/masterDataService";

const LedgerPeriodBanner = ({ apartmentId }) => {
  const [period, setPeriod] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let isMounted = true;
    const fetchPeriod = async () => {
      try {
        const data = await masterDataService.getActiveLedgerPeriod(apartmentId);
        if (isMounted) {
          setPeriod(data);
        }
      } catch (error) {
        console.error("Failed to load active ledger period", error);
      } finally {
        if (isMounted) {
          setLoading(false);
        }
      }
    };

    if (apartmentId) {
      fetchPeriod();
    }

    return () => {
      isMounted = false;
    };
  }, [apartmentId]);

  if (loading || !period) return null;

  return (
    <div className="mb-6 flex items-center justify-between rounded-xl border border-blue-100 bg-blue-50/60 px-5 py-3.5 shadow-xs">
      <div className="flex items-center gap-3">
        <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-blue-600 text-white shadow-xs">
          <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
          </svg>
        </div>
        <div>
          <p className="text-xs font-semibold uppercase tracking-wider text-blue-600">Active Ledger Period</p>
          <p className="text-base font-bold text-slate-800">
            {period.monthName || period.month} / {period.year}
          </p>
        </div>
      </div>
      <span className="rounded-full bg-emerald-100 px-3 py-1 text-xs font-semibold text-emerald-800">
        Active & Open
      </span>
    </div>
  );
};

export default LedgerPeriodBanner;
