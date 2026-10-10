import React, { useState } from "react";
import { toast } from "react-toastify";
import { expenseService } from "../api/expenseService";
import { useAuth } from "../context/AuthContext";
import { usePeriod } from "../context/PeriodContext";
import ConfirmationModal from "../components/ConfirmationModal";

const CheckerInbox = ({ apartmentId: routeApartmentId }) => {
  const { user } = useAuth();
  const { activePeriod } = usePeriod();
  const apartmentId = Number(routeApartmentId ?? user?.apartmentId ?? 1);
  const currentUserId = Number(user?.id ?? user?.userId ?? 0);

  const [drafts, setDrafts] = useState([]);
  const [selectedIds, setSelectedIds] = useState([]);

  const [loading, setLoading] = useState(false);
  const [actionLoading, setActionLoading] = useState(false);
  const [hasSearched, setHasSearched] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  const [modalConfig, setModalConfig] = useState({
    isOpen: false,
    type: "", // "APPROVE" or "REJECT"
    title: "",
  });
  const [rejectReason, setRejectReason] = useState("");

  const handleLoadPending = async () => {
    try {
      setLoading(true);
      setErrorMessage("");

      if (!activePeriod) {
        throw new Error("Active ledger period is not available.");
      }

      const pendingDrafts = await expenseService.getPendingDrafts(apartmentId);

      setDrafts(pendingDrafts);
      setHasSearched(true);
      setSelectedIds([]);
    } catch (error) {
      console.error("Error loading pending expenses:", error);
      setErrorMessage("Failed to fetch pending expense approvals.");
    } finally {
      setLoading(false);
    }
  };

  const getDraftId = (draft) => Number(draft?.id ?? draft?.draftId);

  // Bulk Approval Handler
  const handleApprove = async () => {
    if (selectedIds.length === 0) return;

    try {
      setActionLoading(true);
      await expenseService.approveDrafts(
        apartmentId,
        selectedIds,
        currentUserId,
      );

      setDrafts(
        drafts.filter((draft) => !selectedIds.includes(getDraftId(draft))),
      );
      setSelectedIds([]);
      closeModal();
      toast.success(`${selectedIds.length} expense(s) approved successfully.`);
    } catch (error) {
      if (error.response?.data?.message) {
        setErrorMessage(error.response.data.message);
      } else {
        setErrorMessage("Failed to approve drafts.");
      }
      toast.error("Failed to approve drafts.");
    } finally {
      setActionLoading(false);
    }
  };

  // Bulk Rejection Handler (passes optional/required reason)
  const handleReject = async () => {
    if (selectedIds.length === 0) return;

    try {
      setActionLoading(true);
      // Pass rejectReason to backend
      await expenseService.rejectDrafts(
        apartmentId,
        selectedIds,
        currentUserId,
        rejectReason,
      );

      setDrafts(
        drafts.filter((draft) => !selectedIds.includes(getDraftId(draft))),
      );
      setSelectedIds([]);
      closeModal();
      toast.success(`${selectedIds.length} expense(s) rejected successfully.`);
    } catch (error) {
      if (error.response?.data?.message) {
        setErrorMessage(error.response.data.message);
      } else {
        setErrorMessage("Failed to reject drafts.");
      }
      toast.error("Failed to reject drafts.");
    } finally {
      setActionLoading(false);
    }
  };

  const toggleSelection = (draftId) => {
    const id = Number(draftId);
    if (!Number.isFinite(id)) return;

    setSelectedIds((prev) =>
      prev.includes(id) ? prev.filter((item) => item !== id) : [...prev, id],
    );
  };

  const toggleSelectAll = (e) => {
    const checked = Boolean(e?.target?.checked);
    setSelectedIds(checked ? drafts.map((draft) => getDraftId(draft)) : []);
  };

  const openModal = (type) => {
    setErrorMessage("");
    setRejectReason("");
    setModalConfig({
      isOpen: true,
      type,
      title:
        type === "APPROVE"
          ? "Confirm Expense Approval"
          : "Reject Expense Entries",
    });
  };

  const closeModal = () =>
    setModalConfig({ isOpen: false, type: "", title: "" });

  const LoadingSpinner = () => (
    <svg
      className="animate-spin -ml-1 mr-2 h-4 w-4 text-current inline-block"
      fill="none"
      viewBox="0 0 24 24">
      <circle
        className="opacity-25"
        cx="12"
        cy="12"
        r="10"
        stroke="currentColor"
        strokeWidth="4"></circle>
      <path
        className="opacity-75"
        fill="currentColor"
        d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
    </svg>
  );

  return (
    <div>
      {/* HEADER & CONTROLS */}
      <div className="flex flex-col md:flex-row justify-between items-start md:items-center bg-white p-4 md:p-5 rounded-2xl shadow-sm border border-slate-200 mb-6 gap-4">
        <div>
          <h2 className="text-xl md:text-2xl font-bold text-slate-800">
            Expense Approvals
          </h2>
          <p className="text-sm text-slate-500 mt-1">
            Review and authorize pending community expense drafts
          </p>
        </div>

        <button
          onClick={handleLoadPending}
          disabled={loading}
          className="w-full md:w-auto bg-blue-600 hover:bg-blue-700 text-white px-6 py-2.5 rounded-xl font-medium transition-all shadow-sm flex justify-center items-center disabled:opacity-70 disabled:cursor-not-allowed">
          {loading ? (
            <>
              <LoadingSpinner /> Fetching...
            </>
          ) : (
            "Load Pending Approvals"
          )}
        </button>
      </div>

      {errorMessage && !modalConfig.isOpen && (
        <div className="mb-6 p-4 bg-red-50 border-l-4 border-red-500 text-red-700 rounded-r-xl text-sm font-medium">
          {errorMessage}
        </div>
      )}

      {hasSearched && (
        <div className="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden">
          {activePeriod && (
            <div className="bg-slate-50 px-4 md:px-6 py-3 border-b border-slate-200 flex justify-end items-center">
              <span className="bg-blue-100 text-blue-800 text-xs font-bold px-3 py-1 rounded-full">
                {drafts.length} Pending
              </span>
            </div>
          )}

          {drafts.length === 0 ? (
            <div className="p-12 text-center flex flex-col items-center justify-center">
              <svg
                className="w-16 h-16 text-slate-300 mb-4"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor">
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth="1.5"
                  d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
                />
              </svg>
              <h3 className="text-lg font-medium text-slate-900">
                All caught up!
              </h3>
              <p className="text-slate-500 mt-1">
                No pending expense drafts found.
              </p>
            </div>
          ) : (
            <>
              {/* DESKTOP TABLE */}
              <div className="hidden md:block overflow-x-auto">
                <table className="w-full text-left border-collapse">
                  <thead className="bg-slate-50 border-b border-slate-200">
                    <tr>
                      <th className="p-4 w-12 text-center">
                        <input
                          type="checkbox"
                          className="w-4 h-4 rounded border-slate-300 text-blue-600 focus:ring-blue-500 cursor-pointer"
                          onChange={toggleSelectAll}
                          checked={
                            drafts.length > 0 &&
                            selectedIds.length === drafts.length
                          }
                        />
                      </th>
                      <th className="p-4 text-xs font-semibold text-slate-500 uppercase tracking-wider">
                        Date
                      </th>
                      <th className="p-4 text-xs font-semibold text-slate-500 uppercase tracking-wider">
                        Category
                      </th>
                      <th className="p-4 text-xs font-semibold text-slate-500 uppercase tracking-wider">
                        Amount
                      </th>
                      <th className="p-4 text-xs font-semibold text-slate-500 uppercase tracking-wider">
                        Mode
                      </th>
                      <th className="p-4 text-xs font-semibold text-slate-500 uppercase tracking-wider">
                        Remarks / Ref
                      </th>
                      <th className="p-4 text-xs font-semibold text-slate-500 uppercase tracking-wider">
                        Maker
                      </th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100">
                    {drafts.map((draft) => {
                      const draftId = getDraftId(draft);
                      return (
                        <tr
                          key={draftId}
                          className="hover:bg-blue-50/30 transition-colors">
                          <td className="p-4 text-center">
                            <input
                              type="checkbox"
                              className="w-4 h-4 rounded border-slate-300 text-blue-600 focus:ring-blue-500 cursor-pointer"
                              checked={selectedIds.includes(draftId)}
                              onChange={() => toggleSelection(draftId)}
                            />
                          </td>
                          <td className="p-4 text-slate-600 text-sm">
                            {draft.transactionDate}
                          </td>
                          <td className="p-4 font-semibold text-slate-900 text-sm">
                            {draft.ledgerCategoryName}
                          </td>
                          <td className="p-4 font-bold text-rose-600 text-sm">
                            ₹{Number(draft.amount).toLocaleString("en-IN")}
                          </td>
                          <td className="p-4 text-slate-600 text-sm">
                            {draft.paymentModeName}
                          </td>
                          <td className="p-4 text-slate-600 text-sm">
                            <p className="font-medium">
                              {draft.remarks || "-"}
                            </p>
                            {draft.referenceNumber && (
                              <p className="text-xs text-slate-400">
                                Ref: {draft.referenceNumber}
                              </p>
                            )}
                          </td>
                          <td className="p-4 text-slate-600 text-sm">
                            {draft.createdBy}
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>

              {/* ACTION BAR */}
              <div className="bg-slate-50 border-t border-slate-200 p-4 flex justify-between items-center">
                <span className="text-sm text-slate-600 font-medium">
                  <span className="font-bold text-blue-600">
                    {selectedIds.length}
                  </span>{" "}
                  selected
                </span>

                <div className="flex gap-3">
                  <button
                    onClick={() => openModal("REJECT")}
                    disabled={selectedIds.length === 0}
                    className="px-5 py-2 bg-white hover:bg-red-50 text-red-600 border border-red-200 rounded-xl font-medium disabled:opacity-50 transition-all text-sm shadow-xs">
                    Reject
                  </button>
                  <button
                    onClick={() => openModal("APPROVE")}
                    disabled={selectedIds.length === 0}
                    className="px-5 py-2 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl font-medium disabled:opacity-50 transition-all text-sm shadow-xs">
                    Approve
                  </button>
                </div>
              </div>
            </>
          )}
        </div>
      )}

      <ConfirmationModal
        isOpen={modalConfig.isOpen}
        title={modalConfig.title}
        variant={modalConfig.type === "APPROVE" ? "success" : "danger"}
        errorMessage={errorMessage}
        confirmLabel={
          modalConfig.type === "APPROVE"
            ? "Confirm Approval"
            : "Submit Rejection"
        }
        loadingLabel="Processing..."
        isLoading={actionLoading}
        onCancel={closeModal}
        onConfirm={
          modalConfig.type === "APPROVE" ? handleApprove : handleReject
        }>
        {modalConfig.type === "APPROVE" ? (
          <p className="text-slate-600">
            You are about to approve{" "}
            <strong className="text-slate-900">{selectedIds.length}</strong>{" "}
            expense entries. These will be added to the official ledger.
          </p>
        ) : (
          <div>
            <p className="mb-3 text-sm text-slate-600">
              You are rejecting{" "}
              <strong className="text-slate-900">{selectedIds.length}</strong>{" "}
              expense entries. Please provide a reason:
            </p>
            <textarea
              className="w-full rounded-xl border border-slate-300 p-3 text-sm outline-none focus:ring-2 focus:ring-red-500"
              rows="3"
              placeholder="e.g., Incorrect amount or incorrect category..."
              value={rejectReason}
              onChange={(e) => setRejectReason(e.target.value)}
            />
          </div>
        )}
      </ConfirmationModal>
    </div>
  );
};

export default CheckerInbox;
