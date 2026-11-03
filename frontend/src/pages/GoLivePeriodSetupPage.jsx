import { useEffect, useState } from "react";
import { apartmentService } from "../api/apartmentService";
import { useAuth } from "../context/AuthContext";
import { usePeriod } from "../context/PeriodContext";
import {
  SetupFormField,
  SetupPageHeader,
  SetupStatusMessage,
  SetupSubmitButton,
} from "../components/SetupFormComponents";

const GoLivePeriodSetupPage = () => {
  const { user } = useAuth();
  const { refreshPeriod } = usePeriod();
  const apartmentId = Number(user?.apartmentId);
  const [form, setForm] = useState({ year: "", month: "" });
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const [successMessage, setSuccessMessage] = useState("");

  useEffect(() => {
    let isMounted = true;
    const loadApartment = async () => {
      try {
        if (!Number.isInteger(apartmentId) || apartmentId <= 0) {
          throw new Error("Your session is missing an apartment. Please log in again.");
        }
        const apartment = await apartmentService.getApartmentDetails(apartmentId);
        if (isMounted) {
          setForm({
            year: apartment.goLiveYear ?? "",
            month: apartment.goLiveMonth ?? "",
          });
        }
      } catch (error) {
        if (isMounted) {
          setErrorMessage(
            error.response?.data?.message ||
              error.message ||
              "Failed to load the apartment's starting period.",
          );
        }
      } finally {
        if (isMounted) setIsLoading(false);
      }
    };

    loadApartment();
    return () => {
      isMounted = false;
    };
  }, [apartmentId]);

  const handleSubmit = async (event) => {
    event.preventDefault();
    setErrorMessage("");
    setSuccessMessage("");
    const year = Number(form.year);
    const month = Number(form.month);
    if (!Number.isInteger(year) || year < 2000 || year > 2100) {
      setErrorMessage("Enter a year between 2000 and 2100.");
      return;
    }
    if (!Number.isInteger(month) || month < 1 || month > 12) {
      setErrorMessage("Choose a month from January to December.");
      return;
    }

    try {
      setIsSubmitting(true);
      await apartmentService.updateGoLivePeriod(apartmentId, { year, month });
      await refreshPeriod();
      setForm({ year, month });
      setSuccessMessage("Financial starting period saved.");
    } catch (error) {
      setErrorMessage(
        error.response?.data?.message ||
          "Failed to save the financial starting period.",
      );
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="mx-auto max-w-3xl space-y-6">
      <SetupPageHeader
        eyebrow="One-Time Setup"
        title="Financial Starting Period"
        description="Choose the first month and year in which the apartment will record financial transactions."
      />

      <p className="rounded-xl border border-amber-200 bg-amber-50 p-4 text-sm text-amber-900">
        This period is used as the apartment&apos;s go-live setting. You can
        change it before financial entries are recorded; afterward, it is
        locked to protect the ledger history.
      </p>

      {errorMessage && (
        <SetupStatusMessage>{errorMessage}</SetupStatusMessage>
      )}
      {successMessage && (
        <SetupStatusMessage type="success">{successMessage}</SetupStatusMessage>
      )}

      {isLoading ? (
        <p className="text-sm text-slate-600">Loading apartment settings...</p>
      ) : (
        <form
          onSubmit={handleSubmit}
          className="grid gap-5 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm md:grid-cols-2">
          <SetupFormField label="Starting year">
            <input
              type="number"
              min="2000"
              max="2100"
              required
              value={form.year}
              onChange={(event) =>
                setForm((current) => ({ ...current, year: event.target.value }))
              }
              className="w-full rounded-xl border border-slate-300 px-3 py-2.5"
            />
          </SetupFormField>
          <SetupFormField label="Starting month">
            <select
              required
              value={form.month}
              onChange={(event) =>
                setForm((current) => ({ ...current, month: event.target.value }))
              }
              className="w-full rounded-xl border border-slate-300 bg-white px-3 py-2.5">
              <option value="">Select a month</option>
              {Array.from({ length: 12 }, (_, index) => (
                <option key={index + 1} value={index + 1}>
                  {new Date(2000, index).toLocaleString("en", { month: "long" })}
                </option>
              ))}
            </select>
          </SetupFormField>
          <SetupSubmitButton
            isSubmitting={isSubmitting}
            className="md:col-span-2">
            Save Starting Period
          </SetupSubmitButton>
        </form>
      )}
    </div>
  );
};

export default GoLivePeriodSetupPage;
