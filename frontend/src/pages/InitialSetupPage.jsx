import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { toast } from "react-toastify";
import ConfirmationModal from "../components/ConfirmationModal";
import { apartmentService } from "../api/apartmentService";
import { useAuth } from "../context/AuthContext";

const setupItems = [
  {
    title: "Create flats",
    description:
      "Add the apartment's flats and their owner contact details before recording income or expenses.",
    path: "/admin/setup/flats",
    icon: "🏢",
  },
  {
    title: "Financial starting period",
    description:
      "Set the month and year from which this apartment begins recording financial transactions.",
    path: "/admin/setup/period",
    icon: "📅",
  },
  {
    title: "Opening balance",
    description:
      "Record the balances immediately before the financial starting period, if your apartment has existing financial history.",
    path: "/admin/opening-balance",
    icon: "＋",
  },
];

const InitialSetupPage = () => {
  const navigate = useNavigate();
  const { user, updateUser } = useAuth();
  const [isConfirmOpen, setIsConfirmOpen] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  const handleCompleteSetup = async () => {
    const apartmentId = Number(user?.apartmentId);
    if (!Number.isInteger(apartmentId) || apartmentId <= 0) {
      setErrorMessage("Your session is missing an apartment. Please log in again.");
      return;
    }

    try {
      setIsSubmitting(true);
      setErrorMessage("");
      const apartment =
        await apartmentService.completeInitialSetup(apartmentId);
      updateUser({ isSetupCompleted: apartment.isSetupCompleted });
      toast.success("One-time setup is complete and has been frozen.");
      navigate("/dashboard", { replace: true });
    } catch (error) {
      setErrorMessage(
        error.response?.data?.message ||
          "Unable to complete one-time setup. Please check the required setup steps.",
      );
      setIsConfirmOpen(false);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="mx-auto max-w-5xl space-y-6">
      <header className="border-b border-slate-200 pb-5">
        <p className="text-xs font-semibold uppercase tracking-wider text-slate-500">
          Administration
        </p>
        <h1 className="mt-1 text-2xl font-bold text-slate-900">
          One-Time Setup
        </h1>
        <p className="mt-1 text-sm text-slate-600">
          Complete these initial records for {user?.apartmentName || "your apartment"} before starting
          financial transactions.
        </p>
      </header>

      <div className="grid gap-4 md:grid-cols-2">
        {setupItems.map((item, index) => (
          <Link
            key={item.path}
            to={item.path}
            className="group rounded-2xl border border-slate-200 bg-white p-6 shadow-sm transition hover:-translate-y-0.5 hover:border-blue-300 hover:shadow-md">
            <div className="flex items-start gap-4">
              <span className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl bg-blue-50 text-xl">
                {item.icon}
              </span>
              <div>
                <p className="text-xs font-semibold uppercase tracking-wider text-slate-400">
                  Step {index + 1}
                </p>
                <h2 className="mt-1 text-base font-bold text-slate-900 group-hover:text-blue-700">
                  {item.title}
                </h2>
                <p className="mt-2 text-sm leading-6 text-slate-600">
                  {item.description}
                </p>
              </div>
            </div>
          </Link>
        ))}
      </div>

      <p className="rounded-xl border border-amber-200 bg-amber-50 p-4 text-sm text-amber-900">
        Set up flats and the financial starting period before recording
        transactions. Enter an opening balance only when carrying forward
        existing financial history. The starting period can’t be changed after
        financial entries exist.
      </p>

      {errorMessage && (
        <div
          role="alert"
          className="rounded-xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-800">
          {errorMessage}
        </div>
      )}

      <div className="flex flex-col items-start justify-between gap-4 rounded-2xl border border-emerald-200 bg-emerald-50 p-6 sm:flex-row sm:items-center">
        <div>
          <h2 className="font-bold text-emerald-950">
            Ready to start using the ledger?
          </h2>
          <p className="mt-1 text-sm text-emerald-800">
            Confirming locks the one-time setup screens. Opening balance is
            optional when there is no prior financial history.
          </p>
        </div>
        <button
          type="button"
          onClick={() => {
            setErrorMessage("");
            setIsConfirmOpen(true);
          }}
          className="shrink-0 rounded-xl bg-emerald-700 px-5 py-3 text-sm font-semibold text-white hover:bg-emerald-800">
          Complete &amp; Freeze Setup
        </button>
      </div>

      <ConfirmationModal
        isOpen={isConfirmOpen}
        title="Complete One-Time Setup?"
        variant="success"
        errorMessage={errorMessage}
        confirmLabel="Complete & Freeze"
        loadingLabel="Completing..."
        isLoading={isSubmitting}
        onCancel={() => setIsConfirmOpen(false)}
        onConfirm={handleCompleteSetup}>
        <p className="text-sm leading-relaxed text-slate-600">
          This will mark setup as complete and lock access to flat creation,
          starting-period configuration, and opening-balance entry. This action
          cannot be undone from the application. An opening balance is optional
          if the apartment has no prior financial history.
        </p>
      </ConfirmationModal>
    </div>
  );
};

export default InitialSetupPage;
