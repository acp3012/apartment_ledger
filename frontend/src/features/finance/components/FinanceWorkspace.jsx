import IncomeTab from "../../../components/IncomeTab";
import ExpenseMakerForm from "../../../pages/ExpenseMakerForm";
import ApproveIncomePage from "../../../pages/ApproveIncomePage";
import CheckerInbox from "../../../pages/CheckerInbox";

const FinanceWorkspace = ({
  apartmentId,
  makerId,
  transactionType = "income",
  mode = "entry",
}) => {
  const isIncome = transactionType === "income";
  const isEntry = mode === "entry";
  const typeLabel = isIncome ? "Income" : "Expense";

  const renderContent = () => {
    if (isEntry) {
      return isIncome ? (
        <IncomeTab
          apartmentId={apartmentId}
          makerId={makerId}
        />
      ) : (
        <ExpenseMakerForm
          apartmentId={apartmentId}
          makerId={makerId}
        />
      );
    }

    return isIncome ? (
      <ApproveIncomePage apartmentId={apartmentId} />
    ) : (
      <CheckerInbox apartmentId={apartmentId} />
    );
  };

  return (
    <div className="mx-auto max-w-6xl">
      <header className="mb-6">
        <div className="flex flex-col gap-3 md:flex-row md:items-end md:justify-between">
          <div>
            <p className="mb-2 text-xs font-semibold uppercase tracking-[0.18em] text-slate-500">
              Finance Workspace
            </p>
            <h2 className="text-2xl font-bold tracking-tight text-slate-900">
              {isEntry ? "Record" : "Approve"} {typeLabel}
            </h2>
            <p className="mt-1 text-sm text-slate-600">
              {isEntry
                ? isIncome
                  ? "Add income records for the active ledger period."
                  : "Add expense records for the active ledger period."
                : isIncome
                  ? "Review and approve pending income drafts."
                  : "Review and approve pending expense drafts."}
            </p>
          </div>

          <div className="inline-flex items-center rounded-full border border-slate-200 bg-white p-1 shadow-sm">
            <span className="rounded-full bg-slate-900 px-3 py-1.5 text-xs font-semibold text-white">
              {typeLabel}
            </span>
            <span className="px-3 py-1.5 text-xs font-medium text-slate-600">
              {isEntry ? "Entry" : "Approval"}
            </span>
          </div>
        </div>
      </header>

      <div className="rounded-2xl border border-slate-200 bg-white shadow-[0_18px_45px_rgba(15,23,42,0.04)]">
        {renderContent()}
      </div>
    </div>
  );
};

export default FinanceWorkspace;
