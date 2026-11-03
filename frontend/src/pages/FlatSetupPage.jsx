import { useCallback, useEffect, useState } from "react";
import { apartmentService } from "../api/apartmentService";
import { useAuth } from "../context/AuthContext";
import {
  SetupFormField,
  SetupPageHeader,
  SetupStatusMessage,
  SetupSubmitButton,
} from "../components/SetupFormComponents";

const initialForm = {
  flatNumber: "",
  floorNumber: "",
  ownerName: "",
  ownerMobileNumber: "",
  ownerEmail: "",
};

const FlatSetupPage = () => {
  const { user } = useAuth();
  const apartmentId = Number(user?.apartmentId);
  const [form, setForm] = useState(initialForm);
  const [editingFlatId, setEditingFlatId] = useState(null);
  const [flats, setFlats] = useState([]);
  const [isLoadingFlats, setIsLoadingFlats] = useState(true);
  const [flatsError, setFlatsError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const [successMessage, setSuccessMessage] = useState("");

  const loadFlats = useCallback(async () => {
    if (!Number.isInteger(apartmentId) || apartmentId <= 0) {
      setFlatsError("Your session is missing an apartment. Please log in again.");
      setIsLoadingFlats(false);
      return;
    }

    try {
      setIsLoadingFlats(true);
      setFlatsError("");
      const existingFlats =
        await apartmentService.getFlatsByApartment(apartmentId);
      setFlats(existingFlats);
    } catch (error) {
      setFlatsError(
        error.response?.data?.message || "Failed to load existing flats.",
      );
    } finally {
      setIsLoadingFlats(false);
    }
  }, [apartmentId]);

  useEffect(() => {
    let isMounted = true;

    if (!Number.isInteger(apartmentId) || apartmentId <= 0) {
      Promise.resolve().then(() => {
        if (isMounted) {
          setFlatsError("Your session is missing an apartment. Please log in again.");
          setIsLoadingFlats(false);
        }
      });
      return () => {
        isMounted = false;
      };
    }

    apartmentService
      .getFlatsByApartment(apartmentId)
      .then((existingFlats) => {
        if (isMounted) setFlats(existingFlats);
      })
      .catch((error) => {
        if (isMounted) {
          setFlatsError(
            error.response?.data?.message || "Failed to load existing flats.",
          );
        }
      })
      .finally(() => {
        if (isMounted) setIsLoadingFlats(false);
      });

    return () => {
      isMounted = false;
    };
  }, [apartmentId]);

  const handleChange = (event) => {
    const value =
      event.target.name === "ownerMobileNumber"
        ? event.target.value.replace(/\D/g, "").slice(0, 10)
        : event.target.value;
    setForm((current) => ({ ...current, [event.target.name]: value }));
    setErrorMessage("");
    setSuccessMessage("");
  };

  const handleEdit = (flat) => {
    setEditingFlatId(flat.flatId);
    setForm({
      flatNumber: flat.flatNumber || "",
      floorNumber: flat.floorNumber ?? "",
      ownerName: flat.ownerName || "",
      ownerMobileNumber: flat.ownerMobile || "",
      ownerEmail: flat.ownerEmail || "",
    });
    setErrorMessage("");
    setSuccessMessage("");
  };

  const handleCancelEdit = () => {
    setEditingFlatId(null);
    setForm(initialForm);
    setErrorMessage("");
    setSuccessMessage("");
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setErrorMessage("");
    setSuccessMessage("");

    if (!Number.isInteger(apartmentId) || apartmentId <= 0) {
      setErrorMessage("Your session is missing an apartment. Please log in again.");
      return;
    }

    try {
      setIsSubmitting(true);
      const flatData = {
        ...form,
        floorNumber: form.floorNumber ? Number(form.floorNumber) : null,
      };
      const savedFlat = editingFlatId
        ? await apartmentService.updateFlat(apartmentId, editingFlatId, flatData)
        : await apartmentService.createFlat(apartmentId, flatData);
      setFlats((currentFlats) =>
        [
          ...currentFlats.filter((flat) => flat.flatId !== savedFlat.flatId),
          savedFlat,
        ].sort((first, second) =>
          (first.flatNumber ?? "").localeCompare(second.flatNumber ?? "", undefined, {
            numeric: true,
            sensitivity: "base",
          }),
        ),
      );
      const wasEditing = editingFlatId !== null;
      setEditingFlatId(null);
      setForm(initialForm);
      setSuccessMessage(
        wasEditing ? "Flat updated successfully." : "Flat created successfully.",
      );
    } catch (error) {
      setErrorMessage(
        error.response?.data?.message ||
          `Failed to ${editingFlatId ? "update" : "create"} the flat.`,
      );
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="mx-auto max-w-5xl space-y-6">
      <SetupPageHeader
        eyebrow={user?.isSetupCompleted ? "Administration" : "One-Time Setup"}
        title={
          editingFlatId
            ? "Edit Flat"
            : user?.isSetupCompleted
              ? "Flats"
              : "Create a Flat"
        }
        description={
          user?.isSetupCompleted
            ? "Update registered flat and owner details."
            : "Add or update a flat and its primary owner contact details."
        }
      />

      {errorMessage && (
        <SetupStatusMessage>{errorMessage}</SetupStatusMessage>
      )}
      {successMessage && (
        <SetupStatusMessage type="success">{successMessage}</SetupStatusMessage>
      )}

      {(!user?.isSetupCompleted || editingFlatId) && (
        <form
          onSubmit={handleSubmit}
          className="grid gap-5 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm md:grid-cols-2">
          <SetupFormField label="Flat number">
            <input
              name="flatNumber"
              value={form.flatNumber}
              onChange={handleChange}
              maxLength={10}
              required
              className="w-full rounded-xl border border-slate-300 px-3 py-2.5"
            />
          </SetupFormField>
          <SetupFormField label="Floor number">
            <input
              name="floorNumber"
              type="number"
              value={form.floorNumber}
              onChange={handleChange}
              className="w-full rounded-xl border border-slate-300 px-3 py-2.5"
            />
          </SetupFormField>
          <SetupFormField label="Owner name">
            <input
              name="ownerName"
              value={form.ownerName}
              onChange={handleChange}
              minLength={3}
              maxLength={50}
              required
              className="w-full rounded-xl border border-slate-300 px-3 py-2.5"
            />
          </SetupFormField>
          <SetupFormField label="Owner mobile number">
            <input
              name="ownerMobileNumber"
              type="tel"
              inputMode="numeric"
              pattern="[0-9]{10}"
              value={form.ownerMobileNumber}
              onChange={handleChange}
              minLength={10}
              maxLength={10}
              required
              className="w-full rounded-xl border border-slate-300 px-3 py-2.5"
            />
          </SetupFormField>
          <SetupFormField
            label="Owner email (optional)"
            className="md:col-span-2">
            <input
              name="ownerEmail"
              type="email"
              value={form.ownerEmail}
              onChange={handleChange}
              className="w-full rounded-xl border border-slate-300 px-3 py-2.5"
            />
          </SetupFormField>
          <SetupSubmitButton
            isSubmitting={isSubmitting}
            submittingLabel={
              editingFlatId ? "Saving changes..." : "Creating flat..."
            }
            className="md:col-span-2">
            {editingFlatId ? "Save Changes" : "Create Flat"}
          </SetupSubmitButton>
          {editingFlatId && (
            <button
              type="button"
              onClick={handleCancelEdit}
              disabled={isSubmitting}
              className="rounded-xl border border-slate-300 px-5 py-3 text-sm font-semibold text-slate-700 hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-60 md:col-span-2">
              Cancel Edit
            </button>
          )}
        </form>
      )}

      <section className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
        <div className="flex flex-wrap items-center justify-between gap-3 border-b border-slate-200 px-6 py-4">
          <div>
            <h2 className="text-base font-bold text-slate-900">
              Existing Flats
            </h2>
            <p className="mt-1 text-sm text-slate-500">
              Review registered flats and update their details.
            </p>
          </div>
          <div className="flex items-center gap-3">
            <span className="rounded-full bg-slate-100 px-3 py-1 text-xs font-semibold text-slate-600">
              {flats.length} {flats.length === 1 ? "flat" : "flats"}
            </span>
            <button
              type="button"
              onClick={loadFlats}
              disabled={isLoadingFlats}
              className="rounded-lg border border-slate-300 px-3 py-1.5 text-xs font-semibold text-slate-700 hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-50">
              {isLoadingFlats ? "Refreshing..." : "Refresh"}
            </button>
          </div>
        </div>

        {flatsError && (
          <div
            role="alert"
            className="m-4 rounded-xl bg-rose-50 p-4 text-sm text-rose-800">
            {flatsError}
          </div>
        )}

        {isLoadingFlats ? (
          <p className="px-6 py-8 text-center text-sm text-slate-500">
            Loading flats...
          </p>
        ) : flats.length === 0 ? (
          <p className="px-6 py-8 text-center text-sm text-slate-500">
            No flats have been registered yet.
          </p>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full min-w-[800px] border-collapse text-left text-sm">
              <thead className="bg-slate-50 text-xs uppercase tracking-wide text-slate-500">
                <tr>
                  <th className="px-6 py-3 font-semibold">Flat ID</th>
                  <th className="px-6 py-3 font-semibold">Flat</th>
                  <th className="px-6 py-3 font-semibold">Floor</th>
                  <th className="px-6 py-3 font-semibold">Owner</th>
                  <th className="px-6 py-3 font-semibold">Mobile</th>
                  <th className="px-6 py-3 font-semibold">Email</th>
                  <th className="px-6 py-3 font-semibold">Actions</th>
                </tr>
              </thead>
              <tbody>
                {flats.map((flat) => (
                  <tr
                    key={flat.flatId}
                    className="border-t border-slate-100 text-slate-700">
                    <td className="px-6 py-3 font-semibold text-slate-900">
                      {flat.flatId}
                    </td>
                    <td className="px-6 py-3 font-semibold text-slate-900">
                      {flat.flatNumber || "-"}
                    </td>
                    <td className="px-6 py-3">{flat.floorNumber ?? "-"}</td>
                    <td className="px-6 py-3">{flat.ownerName || "-"}</td>
                    <td className="px-6 py-3">{flat.ownerMobile || "-"}</td>
                    <td className="px-6 py-3">{flat.ownerEmail || "-"}</td>
                    <td className="px-6 py-3">
                      <button
                        type="button"
                        onClick={() => handleEdit(flat)}
                        className="font-semibold text-blue-600 hover:text-blue-800">
                        Edit
                      </button>
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

export default FlatSetupPage;
