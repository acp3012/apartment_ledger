import { useCallback, useEffect, useState } from "react";
import { maintenanceRateService } from "../api/maintenanceRateService";
import { useAuth } from "../context/AuthContext";
import {
  SetupFormField,
  SetupPageHeader,
  SetupStatusMessage,
  SetupSubmitButton,
} from "../components/SetupFormComponents";

const today = new Date();
const currentMonth = `${today.getFullYear()}-${String(
  today.getMonth() + 1,
).padStart(2, "0")}`;

const toMonthStart = (month) => `${month}-01`;

const toMonthEnd = (month) => {
  const [year, monthNumber] = month.split("-").map(Number);
  const lastDay = new Date(year, monthNumber, 0).getDate();
  return `${month}-${String(lastDay).padStart(2, "0")}`;
};

const MaintenanceFeeSetupPage = () => {
  const { user } = useAuth();
  const apartmentId = Number(user?.apartmentId);
  const [rates, setRates] = useState([]);
  const [isLoadingRates, setIsLoadingRates] = useState(true);
  const [ratesError, setRatesError] = useState("");
  const [form, setForm] = useState({
    effectiveFrom: currentMonth,
    effectiveTo: currentMonth,
    fee: "",
  });
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const [successMessage, setSuccessMessage] = useState("");

  const loadRates = useCallback(async () => {
    if (!Number.isInteger(apartmentId) || apartmentId <= 0) {
      setRatesError("Your session is missing an apartment. Please log in again.");
      setIsLoadingRates(false);
      return;
    }

    try {
      setIsLoadingRates(true);
      setRatesError("");
      const existingRates =
        await maintenanceRateService.getApartmentRates(apartmentId);
      setRates(existingRates);
    } catch (error) {
      setRatesError(
        error.response?.data?.message ||
          "Failed to load existing maintenance fee schedules.",
      );
    } finally {
      setIsLoadingRates(false);
    }
  }, [apartmentId]);

  useEffect(() => {
    let isMounted = true;

    if (!Number.isInteger(apartmentId) || apartmentId <= 0) {
      Promise.resolve().then(() => {
        if (isMounted) {
          setRatesError("Your session is missing an apartment. Please log in again.");
          setIsLoadingRates(false);
        }
      });
      return () => {
        isMounted = false;
      };
    }

    maintenanceRateService
      .getApartmentRates(apartmentId)
      .then((existingRates) => {
        if (isMounted) setRates(existingRates);
      })
      .catch((error) => {
        if (isMounted) {
          setRatesError(
            error.response?.data?.message ||
              "Failed to load existing maintenance fee schedules.",
          );
        }
      })
      .finally(() => {
        if (isMounted) setIsLoadingRates(false);
      });

    return () => {
      isMounted = false;
    };
  }, [apartmentId]);

  const handleSubmit = async (event) => {
    event.preventDefault();
    setErrorMessage("");
    setSuccessMessage("");
    if (!form.effectiveFrom || !form.effectiveTo || Number(form.fee) <= 0) {
      setErrorMessage("Choose an effective month range and a fee greater than zero.");
      return;
    }
    if (form.effectiveTo < form.effectiveFrom) {
      setErrorMessage("The ending month must be on or after the starting month.");
      return;
    }

    if (!Number.isInteger(apartmentId) || apartmentId <= 0) {
      setErrorMessage("Your session is missing an apartment. Please log in again.");
      return;
    }

    try {
      setIsSubmitting(true);
      const createdRate = await maintenanceRateService.createApartmentRate(apartmentId, {
        effectiveFrom: toMonthStart(form.effectiveFrom),
        effectiveTo: toMonthEnd(form.effectiveTo),
        fee: Number(form.fee),
      });
      setRates((currentRates) =>
        [createdRate, ...currentRates].sort((first, second) =>
          second.effectiveFrom.localeCompare(first.effectiveFrom),
        ),
      );
      setSuccessMessage("Monthly maintenance fee schedule saved.");
      setForm((current) => ({ ...current, fee: "" }));
    } catch (error) {
      setErrorMessage(
        error.response?.data?.message ||
          "Failed to save the maintenance fee schedule.",
      );
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="mx-auto max-w-5xl space-y-6">
      <SetupPageHeader
        eyebrow="Ongoing Setup"
        title="Monthly Maintenance Fee"
        description="Set one apartment-wide monthly fee for the selected effective month range."
      />

      <p className="rounded-xl border border-blue-200 bg-blue-50 p-4 text-sm text-blue-900">
        The schedule applies to all flats. When the fee changes, add another
        non-overlapping month range.
      </p>

      {errorMessage && (
        <SetupStatusMessage>{errorMessage}</SetupStatusMessage>
      )}
      {successMessage && (
        <SetupStatusMessage type="success">{successMessage}</SetupStatusMessage>
      )}

      <form
        onSubmit={handleSubmit}
        className="grid gap-5 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm md:grid-cols-2">
        <SetupFormField label="Effective from month">
          <input
            type="month"
            required
            value={form.effectiveFrom}
            onChange={(event) =>
              setForm((current) => ({
                ...current,
                effectiveFrom: event.target.value,
              }))
            }
            className="w-full rounded-xl border border-slate-300 px-3 py-2.5"
          />
        </SetupFormField>
        <SetupFormField label="Effective through month">
          <input
            type="month"
            required
            value={form.effectiveTo}
            min={form.effectiveFrom}
            onChange={(event) =>
              setForm((current) => ({
                ...current,
                effectiveTo: event.target.value,
              }))
            }
            className="w-full rounded-xl border border-slate-300 px-3 py-2.5"
          />
        </SetupFormField>
        <SetupFormField label="Monthly fee" className="md:col-span-2">
          <input
            type="number"
            min="0.01"
            step="0.01"
            required
            value={form.fee}
            onChange={(event) =>
              setForm((current) => ({ ...current, fee: event.target.value }))
            }
            className="w-full rounded-xl border border-slate-300 px-3 py-2.5"
          />
        </SetupFormField>
        <SetupSubmitButton
          isSubmitting={isSubmitting}
          submittingLabel="Saving schedule..."
          className="md:col-span-2">
          Save Fee Schedule
        </SetupSubmitButton>
      </form>

      <section className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
        <div className="flex flex-wrap items-center justify-between gap-3 border-b border-slate-200 px-6 py-4">
          <div>
            <h2 className="text-base font-bold text-slate-900">
              Existing Fee Schedules
            </h2>
            <p className="mt-1 text-sm text-slate-500">
              Apartment-wide maintenance fees and their effective periods.
            </p>
          </div>
          <div className="flex items-center gap-3">
            <span className="rounded-full bg-slate-100 px-3 py-1 text-xs font-semibold text-slate-600">
              {rates.length} {rates.length === 1 ? "schedule" : "schedules"}
            </span>
            <button
              type="button"
              onClick={loadRates}
              disabled={isLoadingRates}
              className="rounded-lg border border-slate-300 px-3 py-1.5 text-xs font-semibold text-slate-700 hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-50">
              {isLoadingRates ? "Refreshing..." : "Refresh"}
            </button>
          </div>
        </div>

        {ratesError && (
          <SetupStatusMessage className="m-4">
            {ratesError}
          </SetupStatusMessage>
        )}

        {isLoadingRates ? (
          <p className="px-6 py-8 text-center text-sm text-slate-500">
            Loading fee schedules...
          </p>
        ) : rates.length === 0 ? (
          <p className="px-6 py-8 text-center text-sm text-slate-500">
            No apartment-wide maintenance fee schedules have been added yet.
          </p>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full min-w-[560px] border-collapse text-left text-sm">
              <thead className="bg-slate-50 text-xs uppercase tracking-wide text-slate-500">
                <tr>
                  <th className="px-6 py-3 font-semibold">Effective From</th>
                  <th className="px-6 py-3 font-semibold">Effective Through</th>
                  <th className="px-6 py-3 text-right font-semibold">
                    Monthly Fee
                  </th>
                </tr>
              </thead>
              <tbody>
                {rates.map((rate) => (
                  <tr
                    key={rate.maintenanceRateId}
                    className="border-t border-slate-100 text-slate-700">
                    <td className="px-6 py-3">{rate.effectiveFrom}</td>
                    <td className="px-6 py-3">{rate.effectiveTo || "No end date"}</td>
                    <td className="px-6 py-3 text-right font-semibold text-slate-900">
                      {Number(rate.fee).toFixed(2)}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </div>
  );
};

export default MaintenanceFeeSetupPage;
