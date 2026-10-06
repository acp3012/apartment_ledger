import { useState, useEffect, useCallback } from "react";
import { expenseService } from "../api/expenseService";
import { toast } from "react-toastify";
import { masterDataService } from "../api/masterDataService";
import LedgerPeriodBanner from "../components/LedgerPeriodBanner";

const ExpenseMakerForm = ({ apartmentId, makerId }) => {
  const [categories, setCategories] = useState([]);
  const [paymentModes, setPaymentModes] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isDraftsLoading, setIsDraftsLoading] = useState(false);
  const [drafts, setDrafts] = useState([]);
  const [editingDraftId, setEditingDraftId] = useState(null);
  const [dateBounds, setDateBounds] = useState({ min: "", max: "" });
  const [ledgerPeriod, setLedgerPeriod] = useState({ year: "", month: "" });

  const initialFormState = {
    categoryId: "",
    transactionDate: "",
    amount: "",
    paymentModeId: "",
    referenceNumber: "",
    remarks: "",
  };

  const [formData, setFormData] = useState(initialFormState);

  const fetchDrafts = useCallback(
    async (year, month) => {
      if (!year || !month) return;

      setIsDraftsLoading(true);
      try {
        const data = await expenseService.getPendingDraftsForPeriod(
          apartmentId,
          year,
          month,
        );
        setDrafts(data);
      } catch (error) {
        console.error("Failed to load expense drafts", error);
        toast.error("Failed to load expense drafts.");
      } finally {
        setIsDraftsLoading(false);
      }
    },
    [apartmentId],
  );

  useEffect(() => {
    let isMounted = true;

    const fetchMasterData = async () => {
      try {
        const [cats, modes, ledgerData] = await Promise.all([
          masterDataService.getExpenseCategories(),
          masterDataService.getPaymentModes(),
          masterDataService.getActiveLedgerPeriod(apartmentId),
        ]);

        if (isMounted) {
          const year = ledgerData.year;
          const month = ledgerData.month;
          setLedgerPeriod({ year, month });
          const minDate = `${year}-${String(month).padStart(2, "0")}-01`;
          const lastDay = new Date(year, month, 0).getDate();
          const maxDate = `${year}-${String(month).padStart(2, "0")}-${lastDay}`;

          setDateBounds({ min: minDate, max: maxDate });
          setCategories(cats);
          setPaymentModes(modes);

          const today = new Date().toISOString().split("T")[0];
          const defaultDate =
            today < minDate || today > maxDate ? maxDate : today;

          setFormData((prev) => ({
            ...prev,
            transactionDate: defaultDate,
          }));

          await fetchDrafts(year, month);
        }
      } catch (error) {
        console.error("Failed to load master data", error);
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    };

    fetchMasterData();

    return () => {
      isMounted = false;
    };
  }, [apartmentId, fetchDrafts]);

  const handleChange = (e) => {
    const { name, value } = e.target;

    if (name === "transactionDate") {
      if (!dateBounds.min || !dateBounds.max) {
        setFormData((prev) => ({ ...prev, [name]: value }));
        return;
      }

      if (value < dateBounds.min || value > dateBounds.max) {
        return;
      }
    }

    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsSubmitting(true);

    try {
      const payload = {
        ledgerCategoryId: Number(formData.categoryId),
        transactionDate: formData.transactionDate,
        amount: Number(formData.amount),
        paymentModeId: Number(formData.paymentModeId),
        referenceNumber: formData.referenceNumber,
        remarks: formData.remarks,
        makerId: makerId,
      };

      if (editingDraftId !== null) {
        await expenseService.updateExpenseDraft(
          apartmentId,
          editingDraftId,
          payload,
        );
        toast.success("Expense draft updated successfully!");
        setEditingDraftId(null);
      } else {
        await expenseService.submitExpenseDraft(apartmentId, payload);
        toast.success("Expense draft submitted successfully!");
      }

      setFormData(initialFormState);
      await fetchDrafts(ledgerPeriod.year, ledgerPeriod.month);
    } catch (error) {
      if (error.response?.data?.message) {
        toast.error(error.response.data.message);
      } else {
        toast.error("Failed to submit expense draft.");
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleEdit = (draft) => {
    const normalizedCategoryName = (draft.ledgerCategoryName || draft.categoryName || "").trim().toLowerCase();
    const category = categories.find(
      (item) =>
        String(item.id) === String(draft.ledgerCategoryId) ||
        item.categoryName?.trim().toLowerCase() === normalizedCategoryName
    );

    const normalizedPaymentMode = (draft.paymentModeName || draft.paymentMode || "").trim().toLowerCase();
    const paymentMode = paymentModes.find(
      (item) =>
        String(item.id) === String(draft.paymentModeId) ||
        item.name?.trim().toLowerCase() === normalizedPaymentMode
    );

    setEditingDraftId(draft.draftId ?? draft.id);
    setFormData({
      categoryId: String(category?.id ?? draft.ledgerCategoryId ?? ""),
      transactionDate: draft.transactionDate || "",
      amount: draft.amount ?? "",
      paymentModeId: String(paymentMode?.id ?? draft.paymentModeId ?? ""),
      referenceNumber: draft.referenceNumber || "",
      remarks: draft.remarks || "",
    });
  };

  const handleCancelEdit = () => {
    setEditingDraftId(null);
    setFormData(initialFormState);
  };

  if (isLoading) {
    return <div className="p-6 text-gray-500">Loading expense form...</div>;
  }

  return (
    <div>
      <LedgerPeriodBanner apartmentId={apartmentId} />
      <form
        onSubmit={handleSubmit}
        className="mb-8 grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <div>
          <label className="mb-1 block text-sm font-medium text-gray-700">
            Date *
          </label>
          <input
            type="date"
            name="transactionDate"
            required
            min={dateBounds.min}
            max={dateBounds.max}
            value={formData.transactionDate}
            onChange={handleChange}
            className="h-11 w-full rounded border px-3 text-sm"
          />
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium text-gray-700">
            Category *
          </label>
          <select
            name="categoryId"
            required
            value={formData.categoryId}
            onChange={handleChange}
            className="h-11 w-full rounded border bg-gray-50 px-3 text-sm">
            <option
              value=""
              disabled>
              Select Category
            </option>
            {categories.map((cat) => (
              <option
                key={cat.id}
                value={cat.id}>
                {cat.categoryName}
              </option>
            ))}
          </select>
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium text-gray-700">
            Amount (₹) *
          </label>
          <input
            type="number"
            name="amount"
            min="1"
            step="0.01"
            required
            value={formData.amount}
            onChange={handleChange}
            placeholder="0.00"
            className="h-11 w-full rounded border px-3 text-sm"
          />
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium text-gray-700">
            Payment Mode *
          </label>
          <select
            name="paymentModeId"
            required
            value={formData.paymentModeId}
            onChange={handleChange}
            className="h-11 w-full rounded border px-3 text-sm">
            <option value="">Select Mode</option>
            {paymentModes.map((mode) => (
              <option
                key={mode.id}
                value={mode.id}>
                {mode.name}
              </option>
            ))}
          </select>
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium text-gray-700">
            Reference No.
          </label>
          <input
            type="text"
            name="referenceNumber"
            value={formData.referenceNumber}
            onChange={handleChange}
            placeholder="e.g. UTR123456789"
            className="h-11 w-full rounded border px-3 text-sm"
          />
        </div>

        <div className="sm:col-span-2">
          <label className="mb-1 block text-sm font-medium text-gray-700">
            Remarks
          </label>
          <textarea
            name="remarks"
            rows="1"
            value={formData.remarks}
            onChange={handleChange}
            placeholder="Add notes about this expense"
            className="h-11 min-h-11 w-full resize-none rounded border px-3 py-2 text-sm"
          />
        </div>

        <div className="flex items-end gap-2">
          <button
            type="submit"
            disabled={isSubmitting}
            className={`h-11 flex-1 rounded px-3 text-sm font-medium text-white transition-colors ${
              isSubmitting
                ? "cursor-not-allowed bg-blue-400"
                : "bg-blue-600 hover:bg-blue-700"
            }`}>
            {isSubmitting
              ? "Saving..."
              : editingDraftId !== null
                ? "Update Draft"
                : "Add to Drafts"}
          </button>
          {editingDraftId !== null && (
            <button
              type="button"
              onClick={handleCancelEdit}
              className="h-11 flex-1 rounded bg-gray-500 px-3 text-sm font-medium text-white hover:bg-gray-600">
              Cancel
            </button>
          )}
        </div>
      </form>

      <hr className="my-6" />

      <div>
        <h3 className="mb-4 text-lg font-medium text-gray-800">
          Pending Expense Drafts
        </h3>
        <div className="hidden overflow-hidden rounded-lg border border-gray-200 bg-white shadow lg:block">
          <table className="w-full table-fixed divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="w-[13%] px-3 py-3 text-left text-xs font-medium uppercase text-gray-500">
                  Date
                </th>
                <th className="w-[18%] px-3 py-3 text-left text-xs font-medium uppercase text-gray-500">
                  Category
                </th>
                <th className="w-[12%] px-3 py-3 text-left text-xs font-medium uppercase text-gray-500">
                  Amount
                </th>
                <th className="w-[10%] px-3 py-3 text-left text-xs font-medium uppercase text-gray-500">
                  Mode
                </th>
                <th className="w-[15%] px-3 py-3 text-left text-xs font-medium uppercase text-gray-500">
                  Reference
                </th>
                <th className="w-[24%] px-3 py-3 text-left text-xs font-medium uppercase text-gray-500">
                  Remarks
                </th>
                <th className="w-[8%] px-3 py-3 text-right text-xs font-medium uppercase text-gray-500">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200 bg-white">
              {isDraftsLoading ? (
                <tr>
                  <td
                    colSpan="7"
                    className="px-4 py-4 text-center text-sm text-gray-500">
                    Loading expense drafts...
                  </td>
                </tr>
              ) : drafts.length === 0 ? (
                <tr>
                  <td
                    colSpan="7"
                    className="px-4 py-4 text-center text-sm text-gray-500">
                    No pending drafts found.
                  </td>
                </tr>
              ) : (
                drafts.map((draft) => (
                  <tr key={draft.draftId ?? draft.id}>
                    <td className="break-words px-3 py-4 text-sm text-gray-900">
                      {draft.transactionDate}
                    </td>
                    <td className="break-words px-3 py-4 text-sm text-gray-500">
                      {draft.ledgerCategoryName || draft.categoryName || "-"}
                    </td>
                    <td className="break-words px-3 py-4 text-sm font-medium text-gray-900">
                      ₹{Number(draft.amount).toFixed(2)}
                    </td>
                    <td className="break-words px-3 py-4 text-sm text-gray-500">
                      {draft.paymentModeName || draft.paymentMode || "-"}
                    </td>
                    <td className="break-all px-3 py-4 text-sm text-gray-500">
                      {draft.referenceNumber || "-"}
                    </td>
                    <td className="break-words px-3 py-4 text-sm text-gray-500">
                      {draft.remarks || "-"}
                    </td>
                    <td className="px-3 py-4 text-right text-sm font-medium">
                      <button
                        type="button"
                        onClick={() => handleEdit(draft)}
                        className="text-blue-600 hover:text-blue-900">
                        Edit
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
        <div className="space-y-3 lg:hidden">
          {isDraftsLoading ? (
            <div className="rounded-lg border border-gray-200 bg-white px-4 py-6 text-center text-sm text-gray-500">
              Loading expense drafts...
            </div>
          ) : drafts.length === 0 ? (
            <div className="rounded-lg border border-gray-200 bg-white px-4 py-6 text-center text-sm text-gray-500">
              No pending drafts found.
            </div>
          ) : (
            drafts.map((draft) => (
              <article
                key={draft.draftId ?? draft.id}
                className="rounded-lg border border-gray-200 bg-white p-4 shadow-sm">
                <div className="flex items-start justify-between gap-3">
                  <div className="min-w-0">
                    <h4 className="break-words font-medium text-gray-900">
                      {draft.ledgerCategoryName || draft.categoryName || "Uncategorized"}
                    </h4>
                    <p className="mt-1 text-sm text-gray-500">
                      {draft.transactionDate} · {draft.paymentModeName || draft.paymentMode || "-"}
                    </p>
                  </div>
                  <p className="shrink-0 font-semibold text-gray-900">
                    ₹{Number(draft.amount).toFixed(2)}
                  </p>
                </div>
                {(draft.referenceNumber || draft.remarks) && (
                  <div className="mt-3 space-y-1 border-t border-gray-100 pt-3 text-sm text-gray-600">
                    {draft.referenceNumber && (
                      <p className="break-all">Ref: {draft.referenceNumber}</p>
                    )}
                    {draft.remarks && (
                      <p className="break-words">{draft.remarks}</p>
                    )}
                  </div>
                )}
                <div className="mt-3 flex justify-end">
                  <button
                    type="button"
                    onClick={() => handleEdit(draft)}
                    className="text-sm font-medium text-blue-600 hover:text-blue-900">
                    Edit
                  </button>
                </div>
              </article>
            ))
          )}
        </div>
      </div>
    </div>
  );
};

export default ExpenseMakerForm;
