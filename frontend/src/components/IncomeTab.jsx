import { useState, useEffect, useCallback } from "react";
import { masterDataService } from "../api/masterDataService";
import { apartmentService } from "../api/apartmentService";
import { incomeService } from "../api/incomeService";
import { usePeriod } from "../context/PeriodContext";
import { toast } from "react-toastify";

const IncomeTab = ({ apartmentId, makerId }) => {
  const { activePeriod } = usePeriod();
  const [isOtherIncome, setIsOtherIncome] = useState(false);
  const isMaintenance = !isOtherIncome;

  const [flats, setFlats] = useState([]);
  const [paymentModes, setPaymentModes] = useState([]);
  const [ledgerCategories, setLedgerCategories] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [drafts, setDrafts] = useState([]);

  const [allottedRate, setAllottedRate] = useState("");
  const [editingDraftId, setEditingDraftId] = useState(null);
  const [dateBounds, setDateBounds] = useState({ min: "", max: "" });

  const [formData, setFormData] = useState({
    transactionDate: "",
    flatId: "",
    ledgerCategoryId: "", // Will be set dynamically after categories load
    amount: "",
    paymentModeId: "",
    referenceNumber: "",
    remarks: "",
  });

  const fetchDrafts = useCallback(
    async (loadedCategories = ledgerCategories) => {
      try {
        const data = await incomeService.getPendingDrafts(apartmentId);

        const maintenanceCategoryIds = loadedCategories
          .filter((cat) => cat.isFlatMaintenance)
          .map((cat) => cat.id);

        const filteredData = data.filter((d) =>
          isMaintenance
            ? maintenanceCategoryIds.includes(d.ledgerCategoryId)
            : !maintenanceCategoryIds.includes(d.ledgerCategoryId),
        );

        setDrafts(filteredData.length > 0 ? filteredData : data);
      } catch {
        console.error("Failed to load drafts");
      }
    },
    [apartmentId, isMaintenance, ledgerCategories],
  );

  useEffect(() => {
    const loadDependencies = async () => {
      try {
        if (!activePeriod) {
          throw new Error("Active ledger period is not available.");
        }

        const [flatsData, modesData, categoriesData] = await Promise.all([
          apartmentService.getFlatsByApartment(apartmentId),
          masterDataService.getPaymentModes(),
          masterDataService.getIncomeCategories(),
        ]);

        setFlats(flatsData);
        setPaymentModes(modesData);
        setLedgerCategories(categoriesData);

        // Date Bounds Setup
        const year = activePeriod.year;
        const month = activePeriod.month;
        const minDate = `${year}-${String(month).padStart(2, "0")}-01`;
        const lastDay = new Date(year, month, 0).getDate();
        const maxDate = `${year}-${String(month).padStart(2, "0")}-${lastDay}`;
        setDateBounds({ min: minDate, max: maxDate });

        // Find the default maintenance category ID
        const defaultMaintenanceId =
          categoriesData.find((c) => c.isFlatMaintenance)?.id || "";

        const today = new Date().toISOString().split("T")[0];
        setFormData((prev) => ({
          ...prev,
          transactionDate: today < minDate || today > maxDate ? maxDate : today,
          ledgerCategoryId: isMaintenance ? defaultMaintenanceId : "",
        }));

        await fetchDrafts(categoriesData);
      } catch {
        toast.error("Failed to load form dependencies.");
      } finally {
        setIsLoading(false);
      }
    };
    loadDependencies();
  }, [activePeriod, apartmentId, fetchDrafts, isMaintenance]);

  useEffect(() => {
    const fetchDynamicRate = async () => {
      if (isMaintenance && formData.flatId && formData.transactionDate) {
        try {
          const data = await apartmentService.getApplicableRate(
            apartmentId,
            formData.flatId,
            formData.transactionDate,
          );
          setAllottedRate(data.fee);
          setFormData((prev) =>
            prev.amount ? prev : { ...prev, amount: data.fee },
          );
        } catch {
          setAllottedRate("Not Found");
        }
      } else {
        setAllottedRate("");
      }
    };
    fetchDynamicRate();
  }, [formData.flatId, formData.transactionDate, apartmentId, isMaintenance]);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleToggleMode = (e) => {
    const isOther = e.target.checked;
    setIsOtherIncome(isOther);

    // Find dynamic maintenance ID
    const defaultMaintenanceId =
      ledgerCategories.find((c) => c.isFlatMaintenance)?.id || "";

    setFormData((prev) => ({
      ...prev,
      ledgerCategoryId: isOther ? "" : defaultMaintenanceId,
      flatId: "",
      amount: "",
      allottedRate: "",
    }));
  };

  const handleAddOrUpdate = async (e) => {
    e.preventDefault();

    const payload = {
      flatId: formData.flatId ? formData.flatId : null,
      ledgerCategoryId: formData.ledgerCategoryId,
      transactionDate: formData.transactionDate,
      amount: parseFloat(formData.amount),
      paymentModeId: formData.paymentModeId,
      referenceNumber: formData.referenceNumber,
      remarks: formData.remarks,
      makerId: makerId,
    };

    try {
      if (editingDraftId) {
        await incomeService.updateIncomeDraft(
          apartmentId,
          editingDraftId,
          payload,
        );
        toast.success("Draft updated successfully!");
        setEditingDraftId(null);
      } else {
        await incomeService.createIncomeDraft(apartmentId, payload);
        toast.success("Income added to drafts successfully!");
      }

      const defaultMaintenanceId =
        ledgerCategories.find((c) => c.isFlatMaintenance)?.id || "";

      setFormData((prev) => ({
        ...prev,
        flatId: "",
        ledgerCategoryId: isMaintenance ? defaultMaintenanceId : "",
        amount: "",
        paymentModeId: "",
        referenceNumber: "",
        remarks: "",
      }));
      setAllottedRate("");
      await fetchDrafts();
    } catch {
      toast.error(
        editingDraftId ? "Failed to update draft." : "Failed to save draft.",
      );
    }
  };

  const handleApprove = async (draftId) => {
    try {
      await incomeService.approveDraft(apartmentId, draftId);
      toast.success("Draft approved successfully!");
      await fetchDrafts();
    } catch {
      toast.error("Failed to approve draft.");
    }
  };

  const handleEdit = (draft) => {
    setEditingDraftId(draft.draftId);
    const defaultMaintenanceId =
      ledgerCategories.find((c) => c.isFlatMaintenance)?.id || "";

    setFormData({
      transactionDate: draft.transactionDate || "",
      flatId: draft.flatId || "",
      ledgerCategoryId:
        draft.ledgerCategoryId || (isMaintenance ? defaultMaintenanceId : ""),
      amount: draft.amount || "",
      paymentModeId: draft.paymentModeId || "",
      referenceNumber: draft.referenceNumber || "",
      remarks: draft.remarks || "",
    });
  };

  if (isLoading)
    return <div className="p-4 text-gray-500">Loading form...</div>;

  return (
    <div>
      <div className="mb-6 flex items-center bg-blue-50 p-3 rounded-lg border border-blue-100 w-max">
        <input
          type="checkbox"
          id="otherIncomeToggle"
          checked={isOtherIncome}
          onChange={handleToggleMode}
          className="h-4 w-4 text-blue-600 border-gray-300 rounded cursor-pointer"
        />
        <label
          htmlFor="otherIncomeToggle"
          className="ml-2 block text-sm text-blue-900 font-medium cursor-pointer">
          Record as Other/General Income
        </label>
      </div>

      <form
        onSubmit={handleAddOrUpdate}
        className="mb-8 grid grid-cols-1 gap-4 px-4 pt-4 md:grid-cols-4">
        <div>
          <label className="mb-1 block text-sm font-semibold text-slate-800">
            Transaction Date *
          </label>
          <input
            type="date"
            name="transactionDate"
            value={formData.transactionDate}
            onChange={handleChange}
            min={dateBounds.min}
            max={dateBounds.max}
            required
            className="h-11 w-full rounded-md border border-slate-400 bg-white px-3 text-sm font-medium text-slate-900 shadow-xs focus:border-blue-600 focus:outline-none focus:ring-2 focus:ring-blue-100"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Category *
          </label>
          <select
            name="ledgerCategoryId"
            value={formData.ledgerCategoryId}
            onChange={handleChange}
            required
            className="w-full border rounded p-2 bg-gray-50">
            {!isMaintenance && <option value="">Select Category</option>}
            {ledgerCategories
              // Data-driven filter based on your boolean flag
              .filter((cat) =>
                isMaintenance ? cat.isFlatMaintenance : !cat.isFlatMaintenance,
              )
              .map((cat) => (
                <option
                  key={cat.id}
                  value={cat.id}>
                  {cat.categoryName}
                </option>
              ))}
          </select>
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Flat Number {isMaintenance ? "*" : ""}
          </label>
          <select
            name="flatId"
            value={formData.flatId}
            onChange={handleChange}
            required={isMaintenance}
            className="w-full border rounded p-2">
            <option value="">
              {isMaintenance ? "Select Flat" : "Select Flat (Optional)"}
            </option>
            {flats.map((flat) => (
              <option
                key={flat.id || flat.flatId}
                value={flat.id || flat.flatId}>
                {flat.flatNumber}
              </option>
            ))}
          </select>
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Allotted Fee (₹)
          </label>
          <input
            type="text"
            value={isMaintenance ? allottedRate : "N/A"}
            disabled
            className="w-full border rounded p-2 bg-gray-100 text-gray-500 cursor-not-allowed"
            placeholder="Auto-populated"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Received Amount (₹) *
          </label>
          <input
            type="number"
            name="amount"
            value={formData.amount}
            onChange={handleChange}
            required
            className="w-full border rounded p-2"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Payment Mode *
          </label>
          <select
            name="paymentModeId"
            value={formData.paymentModeId}
            onChange={handleChange}
            required
            className="w-full border rounded p-2">
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
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Reference No.
          </label>
          <input
            type="text"
            name="referenceNumber"
            value={formData.referenceNumber}
            onChange={handleChange}
            className="w-full border rounded p-2"
          />
        </div>

        <div className="flex items-end">
          <button
            type="submit"
            className="w-full bg-blue-600 text-white p-2 rounded hover:bg-blue-700 font-medium">
            {editingDraftId ? "Update Draft" : "Add to Drafts"}
          </button>
          {editingDraftId && (
            <button
              type="button"
              onClick={() => {
                setEditingDraftId(null);
                const defaultMaintenanceId =
                  ledgerCategories.find((c) => c.isFlatMaintenance)?.id || "";
                setFormData((prev) => ({
                  ...prev,
                  amount: "",
                  referenceNumber: "",
                  remarks: "",
                  flatId: "",
                  paymentModeId: "",
                  ledgerCategoryId: isMaintenance ? defaultMaintenanceId : "",
                }));
              }}
              className="ml-2 w-full bg-gray-500 text-white p-2 rounded hover:bg-gray-600 font-medium">
              Cancel
            </button>
          )}
        </div>
      </form>

      <hr className="my-6" />

      {/* Grid Section */}
      <div>
        <h3 className="text-lg font-medium text-gray-800 mb-4">Drafts</h3>
        <div className="overflow-x-auto bg-white rounded-lg shadow border border-gray-200">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                  Date
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                  Flat
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                  Category
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                  Amount
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                  Mode
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                  Status
                </th>
                <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {drafts.length === 0 ? (
                <tr>
                  <td
                    colSpan="7"
                    className="px-6 py-4 text-center text-sm text-gray-500">
                    No pending drafts found.
                  </td>
                </tr>
              ) : (
                drafts.map((draft) => {
                  // Derive category text dynamically based on the ID in the draft
                  const isDraftMaintenance = ledgerCategories.find(
                    (c) => c.id === draft.ledgerCategoryId,
                  )?.isFlatMaintenance;

                  return (
                    <tr key={draft.draftId}>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                        {draft.transactionDate}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                        {draft.flatNumber || (
                          <span className="text-gray-400 font-normal">N/A</span>
                        )}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                        {draft.ledgerCategoryName ||
                          (isDraftMaintenance ? "Maintenance" : "General")}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                        ₹{draft.amount}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                        {draft.paymentModeName}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span
                          className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${
                            draft.approvalStatus === "APPROVED"
                              ? "bg-green-100 text-green-800"
                              : "bg-yellow-100 text-yellow-800"
                          }`}>
                          {draft.approvalStatus || "Pending"}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                        {draft.approvalStatus !== "APPROVED" ? (
                          <div className="flex justify-end space-x-3">
                            <button
                              type="button"
                              onClick={() => handleEdit(draft)}
                              className="text-blue-600 hover:text-blue-900">
                              Edit
                            </button>
                            <button
                              type="button"
                              onClick={() => handleApprove(draft.draftId)}
                              className="text-green-600 hover:text-green-900 font-bold">
                              Approve
                            </button>
                          </div>
                        ) : (
                          <span className="text-gray-400 italic">Locked</span>
                        )}
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

export default IncomeTab;
