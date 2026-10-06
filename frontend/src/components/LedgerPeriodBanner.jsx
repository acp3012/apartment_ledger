import { usePeriod } from "../context/PeriodContext";

const LedgerPeriodBanner = () => {
  const { activePeriod, loading } = usePeriod();

  if (loading || !activePeriod) return null;

  return (
    <div className="mb-6 flex items-center justify-between rounded-xl border border-blue-100 bg-blue-50/60 px-5 py-3.5 shadow-xs">
      <div className="flex items-center gap-3">
        <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-blue-600 text-white shadow-xs">
          <svg
            className="h-5 w-5"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor">
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth="2"
              d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"
            />
          </svg>
        </div>
        <div>
          <p className="text-xs font-semibold uppercase tracking-wider text-blue-600">
            Active Ledger Period
          </p>
          <p className="text-base font-bold text-slate-800">
            {activePeriod.monthName || activePeriod.month} / {activePeriod.year}
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
