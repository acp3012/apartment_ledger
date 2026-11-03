import { useEffect, useState } from "react";
import { apartmentService } from "../api/apartmentService";
import { ledgerService } from "../api/ledgerService";
import { useAuth } from "../context/AuthContext";
import {
  SetupFormField,
  SetupPageHeader,
  SetupStatusMessage,
  SetupSubmitButton,
} from "../components/SetupFormComponents";

const initialForm = {
  year: "",
  month: "",
  openingBalance: "",
  income: "",
  expense: "",
};

const OpeningBalancePage = () => {
  const { user } = useAuth();
  const apartmentId = Number(user?.apartmentId);
  const currentYear = new Date().getFullYear();
  const [form, setForm] = useState(initialForm);
  const [goLivePeriod, setGoLivePeriod] = useState(null);
  const [isLoadingApartment, setIsLoadingApartment] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const [successMessage, setSuccessMessage] = useState("");
  const maxAllowedYear = goLivePeriod
    ? Math.min(goLivePeriod.year, currentYear)
    : currentYear;

  useEffect(() => {
    let isMounted = true;

    const loadApartment = async () => {
      try {
        if (!Number.isFinite(apartmentId) || apartmentId <= 0) {
          throw new Error(
            "Your session is missing an apartment. Please log in again.",
          );
        }

        const apartment =
          await apartmentService.getApartmentDetails(apartmentId);
        const year = Number(apartment?.goLiveYear);
        const month = Number(apartment?.goLiveMonth);

        if (
          !Number.isInteger(year) ||
          year < 1 ||
          !Number.isInteger(month) ||
          month < 1 ||
          month > 12
        ) {
          throw new Error("The apartment go-live period is not configured.");
        }

        if (isMounted) setGoLivePeriod({ year, month });
      } catch (error) {
        if (isMounted) {
          setErrorMessage(
            error.response?.data?.message ||
              error.message ||
              "Failed to load apartment settings.",
          );
        }
      } finally {
        if (isMounted) setIsLoadingApartment(false);
      }
    };

    loadApartment();
    return () => {
      isMounted = false;
    };
  }, [apartmentId]);

  const handleChange = (event) => {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
    setErrorMessage("");
    setSuccessMessage("");
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setErrorMessage("");
    setSuccessMessage("");

    if (!Number.isFinite(apartmentId) || apartmentId <= 0) {
      setErrorMessage(
        "Your session is missing an apartment. Please log in again.",
      );
      return;
    }

    if (!goLivePeriod) {
      setErrorMessage("Apartment go-live settings are not available.");
      return;
    }

    const numericValues = [
      form.year,
      form.month,
      form.openingBalance,
      form.income,
      form.expense,
    ].map(Number);

    if (
      Object.values(form).some((value) => value === "") ||
      numericValues.some((value) => !Number.isFinite(value))
    ) {
      setErrorMessage("Enter a valid period and amounts for every field.");
      return;
    }

    const [year, month, openingBalance, income, expense] = numericValues;
    if (
      !Number.isInteger(year) ||
      year < 1 ||
      !Number.isInteger(month) ||
      month < 1 ||
      month > 12
    ) {
      setErrorMessage("Enter a valid year and a month from 1 to 12.");
      return;
    }

    if (year > maxAllowedYear) {
      setErrorMessage("Opening balances cannot be entered for a future year.");
      return;
    }

    if (
      year > goLivePeriod.year ||
      (year === goLivePeriod.year && month >= goLivePeriod.month)
    ) {
      setErrorMessage(
        "Opening balances can only be entered for periods before the go-live period.",
      );
      return;
    }

    try {
      setIsSubmitting(true);
      await ledgerService.createOpeningBalance(apartmentId, {
        year,
        month,
        openingBalance,
        income,
        expense,
      });
      setSuccessMessage("Opening balance saved successfully.");
      setForm(initialForm);
    } catch (error) {
      setErrorMessage(
        error.response?.data?.message || "Failed to save the opening balance.",
      );
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="mx-auto max-w-3xl space-y-6">
      <SetupPageHeader
        eyebrow="One-Time Setup"
        title="Opening Balance"
        description={user?.apartmentName || "Apartment"}
      />

      {isLoadingApartment ? (
        <p className="text-sm text-slate-600">
          Loading apartment go-live settings...
        </p>
      ) : goLivePeriod ? (
        <div className="border-l-4 border-blue-600 bg-blue-50 p-4 text-sm text-blue-900">
          This apartment goes live in{" "}
          <strong>
            {new Date(2000, goLivePeriod.month - 1).toLocaleString("en", {
              month: "long",
            })}{" "}
            {goLivePeriod.year}
          </strong>
          . Opening balances may only be entered for earlier periods.
        </div>
      ) : null}

      {errorMessage && (
        <SetupStatusMessage className="border-l-4 border-rose-500">
          {errorMessage}
        </SetupStatusMessage>
      )}
      {successMessage && (
        <SetupStatusMessage
          type="success"
          className="border-l-4 border-emerald-500">
          {successMessage}
        </SetupStatusMessage>
      )}

      <form
        onSubmit={handleSubmit}
        className="max-w-xl space-y-5">
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <SetupFormField label="Year">
            <input
              name="year"
              type="number"
              min="1"
              max={maxAllowedYear}
              step="1"
              required
              value={form.year}
              onChange={handleChange}
              className="w-full rounded-md border border-slate-300 bg-white px-3 py-2 text-slate-900 outline-none focus:border-blue-600 focus:ring-2 focus:ring-blue-100"
            />
          </SetupFormField>
          <SetupFormField label="Month">
            <select
              name="month"
              required
              value={form.month}
              onChange={handleChange}
              className="w-full rounded-md border border-slate-300 bg-white px-3 py-2 text-slate-900 outline-none focus:border-blue-600 focus:ring-2 focus:ring-blue-100">
              <option value="">Select month</option>
              {Array.from({ length: 12 }, (_, index) => index + 1).map(
                (month) => (
                  <option
                    key={month}
                    value={month}
                    disabled={
                      Boolean(goLivePeriod) &&
                      Number(form.year) === goLivePeriod.year &&
                      month >= goLivePeriod.month
                    }>
                    {new Date(2000, month - 1).toLocaleString("en", {
                      month: "long",
                    })}
                  </option>
                ),
              )}
            </select>
          </SetupFormField>
        </div>

        {[
          ["openingBalance", "Opening Balance"],
          ["income", "Income"],
          ["expense", "Expense"],
        ].map(([name, label]) => (
          <SetupFormField
            key={name}
            label={label}>
            <input
              name={name}
              type="number"
              step="0.01"
              required
              value={form[name]}
              onChange={handleChange}
              className="w-full rounded-md border border-slate-300 bg-white px-3 py-2 text-slate-900 outline-none focus:border-blue-600 focus:ring-2 focus:ring-blue-100"
            />
          </SetupFormField>
        ))}

        <SetupSubmitButton
          isSubmitting={isSubmitting}
          disabled={!Number.isFinite(apartmentId) || apartmentId <= 0}>
          Save Opening Balance
        </SetupSubmitButton>
      </form>
    </div>
  );
};

export default OpeningBalancePage;
