import React, { useState } from "react";
import { useParams } from "react-router-dom";
import { toast } from "react-toastify";
import { incomeService } from "../api/incomeService";
import { masterDataService } from "../api/masterDataService";
import { useAuth } from "../context/AuthContext";

const ApproveIncomePage = ({ apartmentId: routeApartmentId }) => {
  const { apartmentId: routeParamId } = useParams();
  const { user } = useAuth();

  const apartmentId = Number(
    routeApartmentId ??
      routeParamId ??
      user?.apartmentId ??
      user?.apartment_id ??
      0,
  );
  const currentUserId = Number(user?.id ?? user?.userId ?? 0);

  // 2. Core State
  const [drafts, setDrafts] = useState([]);
  const [selectedIds, setSelectedIds] = useState([]);
  const [activePeriod, setActivePeriod] = useState(null);

  // 3. UI Status State
  const [loading, setLoading] = useState(false);
  const [actionLoading, setActionLoading] = useState(false);
  const [hasSearched, setHasSearched] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  // 4. Modal State
  const [modalConfig, setModalConfig] = useState({
    isOpen: false,
    type: "",
    title: "",
  });
  const [rejectReason, setRejectReason] = useState("");

  // --- API CALLS ---

  const hasValidSession = () => {
    if (!Number.isFinite(apartmentId) || apartmentId <= 0) {
      setErrorMessage(
        "Session is missing the apartment ID. Please log in again.",
      );
      return false;
    }

    if (!Number.isFinite(currentUserId) || currentUserId <= 0) {
      setErrorMessage("Session is missing the user ID. Please log in again.");
      return false;
    }

    return true;
  };

  const handleLoadPending = async () => {
    if (!hasValidSession()) return;

    try {
      setLoading(true);
      setErrorMessage("");

      const period = await masterDataService.getActiveLedgerPeriod(apartmentId);
      setActivePeriod(period);

      const pendingDrafts = await incomeService.getPendingDrafts(
        apartmentId,
        period.year,
        period.month,
      );
      setDrafts(pendingDrafts);
      setHasSearched(true);
      setSelectedIds([]);
    } catch (error) {
      console.error("Error loading pending incomes:", error);
      setErrorMessage(
        "Failed to fetch pending approvals. Please check your connection.",
      );
    } finally {
      setLoading(false);
    }
  };

  const getDraftId = (draft) => {
    const rawId = draft?.draftId ?? draft?.id ?? draft?.incomeDraftId;
    const parsed = Number(rawId);
    return Number.isFinite(parsed) ? parsed : null;
  };

  const getDraftKey = (draft, index) => {
    const draftId = getDraftId(draft);
    if (draftId !== null) return `draft-${draftId}`;

    return `draft-fallback-${draft?.transactionDate ?? "date"}-${draft?.flatNumber ?? "flat"}-${draft?.amount ?? 0}-${index}`;
  };

  const executeApproval = async () => {
    if (!hasValidSession()) return;

    try {
      setActionLoading(true);
      console.log("Approval payload:", {
        apartmentId,
        draftIds: selectedIds,
        approverId: user.id,
      });
      await incomeService.approveDrafts(
        apartmentId,
        selectedIds,
        currentUserId,
      );

      setDrafts(
        drafts.filter((draft) => !selectedIds.includes(getDraftId(draft))),
      );
      setSelectedIds([]);
      closeModal();
      toast.success("Selected incomes approved successfully.");
    } catch (error) {
      setErrorMessage("Failed to approve drafts. Please try again.");
      toast.error("Failed to approve drafts. Please try again.");
    } finally {
      setActionLoading(false);
    }
  };

  const executeRejection = async () => {
    if (!hasValidSession()) return;

    if (!rejectReason.trim()) {
      setErrorMessage("Please provide a rejection reason.");
      return;
    }

    try {
      setActionLoading(true);
      console.log("Rejection payload:", {
        apartmentId,
        draftIds: selectedIds,
        approverId: currentUserId,
        rejectReason,
      });
      await incomeService.rejectDrafts(
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
      toast.success("Selected incomes rejected and sent back to the maker.");
    } catch (error) {
      setErrorMessage("Failed to reject drafts.");
      toast.error("Failed to reject drafts.");
    } finally {
      setActionLoading(false);
    }
  };

  // --- EVENT HANDLERS ---

  const normalizeDraftId = (value) => {
    const parsed = Number(value);
    return Number.isFinite(parsed) ? parsed : null;
  };

  const toggleSelection = (draftId) => {
    const id = normalizeDraftId(draftId);
    if (id === null) return;

    setSelectedIds((prev) => {
      const normalizedPrev = prev
        .map((item) => normalizeDraftId(item))
        .filter((item) => item !== null);

      return normalizedPrev.includes(id)
        ? normalizedPrev.filter((item) => item !== id)
        : [...normalizedPrev, id];
    });
  };

  const toggleSelectAll = (e) => {
    const checked = Boolean(e?.target?.checked);
    setSelectedIds(
      checked
        ? drafts.map((draft) => getDraftId(draft)).filter((id) => id !== null)
        : [],
    );
  };

  const openModal = (type) => {
    setErrorMessage("");
    setRejectReason("");
    setModalConfig({
      isOpen: true,
      type,
      title: type === "APPROVE" ? "Confirm Approval" : "Reject Entries",
    });
  };

  const closeModal = () =>
    setModalConfig({ isOpen: false, type: "", title: "" });

  // --- RENDER HELPERS ---

  // SVG Spinner for loading states
  const LoadingSpinner = () => (
    <svg
      className="animate-spin -ml-1 mr-2 h-4 w-4 text-current inline-block"
      xmlns="http://www.w3.org/2000/svg"
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
    <div className="p-4 md:p-6 max-w-7xl mx-auto pb-24 md:pb-6">
      {/* HEADER & CONTROLS */}
      <div className="flex flex-col md:flex-row justify-between items-start md:items-center bg-white p-4 md:p-5 rounded-xl shadow-sm border border-gray-200 mb-6 gap-4">
        <div>
          <h2 className="text-xl md:text-2xl font-bold text-gray-800">
            Income Approvals
          </h2>
          <p className="text-sm text-gray-500 mt-1">
            Maker-Checker authorization queue
          </p>
        </div>

        <button
          onClick={handleLoadPending}
          disabled={loading}
          className="w-full md:w-auto bg-indigo-600 hover:bg-indigo-700 text-white px-6 py-2.5 rounded-lg font-medium transition-all shadow-sm flex justify-center items-center disabled:opacity-70 disabled:cursor-not-allowed">
          {loading ? (
            <>
              <LoadingSpinner /> Fetching...
            </>
          ) : (
            "Load Pending Approvals"
          )}
        </button>
      </div>

      {/* ERROR MESSAGE STRIP */}
      {errorMessage && !modalConfig.isOpen && (
        <div className="mb-6 p-4 bg-red-50 border-l-4 border-red-500 text-red-700 rounded-r-md text-sm font-medium">
          {errorMessage}
        </div>
      )}

      {/* MAIN CONTENT AREA */}
      {hasSearched && (
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
          {/* Active Period Banner */}
          {activePeriod && (
            <div className="bg-slate-50 px-4 md:px-6 py-3 border-b border-gray-200 flex flex-col md:flex-row justify-between items-center gap-2">
              <span className="text-slate-700 font-medium text-sm md:text-base">
                Ledger Period:{" "}
                <span className="font-bold">
                  {activePeriod.month} / {activePeriod.year}
                </span>
              </span>
              <span className="bg-indigo-100 text-indigo-800 text-xs font-bold px-3 py-1 rounded-full">
                {drafts.length} Pending
              </span>
            </div>
          )}

          {drafts.length === 0 ? (
            <div className="p-12 text-center flex flex-col items-center justify-center">
              <svg
                className="w-16 h-16 text-gray-300 mb-4"
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
              <h3 className="text-lg font-medium text-gray-900">
                All caught up!
              </h3>
              <p className="text-gray-500 mt-1">
                No pending drafts found for this period.
              </p>
            </div>
          ) : (
            <>
              {/* DESKTOP VIEW: Table (Hidden on mobile) */}
              <div className="hidden md:block overflow-x-auto">
                <table className="w-full text-left border-collapse">
                  <thead className="bg-gray-50 border-b border-gray-200">
                    <tr>
                      <th className="p-4 w-12 text-center">
                        <input
                          type="checkbox"
                          className="w-4 h-4 rounded border-gray-300 text-indigo-600 focus:ring-indigo-500 cursor-pointer"
                          onChange={(e) => {
                            e.stopPropagation();
                            toggleSelectAll(e);
                          }}
                          checked={
                            drafts.length > 0 &&
                            selectedIds.length === drafts.length
                          }
                        />
                      </th>
                      <th className="p-4 text-sm font-semibold text-gray-600 uppercase tracking-wider">
                        Date
                      </th>
                      <th className="p-4 text-sm font-semibold text-gray-600 uppercase tracking-wider">
                        Flat/Unit
                      </th>
                      <th className="p-4 text-sm font-semibold text-gray-600 uppercase tracking-wider">
                        Amount
                      </th>
                      <th className="p-4 text-sm font-semibold text-gray-600 uppercase tracking-wider">
                        Status
                      </th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-100">
                    {drafts.map((draft, index) => {
                      const draftId = getDraftId(draft);

                      return (
                        <tr
                          key={getDraftKey(draft, index)}
                          className="hover:bg-indigo-50/30 transition-colors">
                          <td className="p-4 text-center">
                            <input
                              type="checkbox"
                              className="w-4 h-4 rounded border-gray-300 text-indigo-600 focus:ring-indigo-500 cursor-pointer"
                              checked={
                                draftId !== null &&
                                selectedIds.includes(draftId)
                              }
                              onChange={() => toggleSelection(draftId)}
                            />
                          </td>
                          <td className="p-4 text-gray-600">
                            {draft.transactionDate}
                          </td>
                          <td className="p-4 font-medium text-gray-900">
                            {draft.flatNumber || "N/A"}
                          </td>
                          <td className="p-4 font-semibold text-emerald-600">
                            ₹{draft.amount.toLocaleString("en-IN")}
                          </td>
                          <td className="p-4">
                            <span className="px-2.5 py-1 text-xs font-medium bg-amber-100 text-amber-800 rounded-md border border-amber-200">
                              Pending
                            </span>
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
              {/* MOBILE VIEW: Cards (Hidden on desktop) */}
              <div className="md:hidden flex flex-col p-4 space-y-3 bg-gray-50">
                <div className="flex items-center pb-2 px-1">
                  <input
                    type="checkbox"
                    id="selectAllMobile"
                    className="w-5 h-5 rounded border-gray-300 text-indigo-600 mr-3"
                    onChange={(e) => {
                      e.stopPropagation();
                      toggleSelectAll(e);
                    }}
                    checked={
                      drafts.length > 0 && selectedIds.length === drafts.length
                    }
                  />
                  <label
                    htmlFor="selectAllMobile"
                    className="text-sm font-medium text-gray-700">
                    Select All {drafts.length} items
                  </label>
                </div>

                {drafts.map((draft, index) => {
                  const draftId = getDraftId(draft);

                  return (
                    <div
                      key={getDraftKey(draft, index)}
                      className={`bg-white p-4 rounded-xl shadow-sm border transition-all ${draftId !== null && selectedIds.includes(draftId) ? "border-indigo-500 ring-1 ring-indigo-500" : "border-gray-200"}`}>
                      <div className="flex justify-between items-start mb-2">
                        <div className="flex items-center gap-3">
                          <input
                            type="checkbox"
                            className="w-5 h-5 rounded border-gray-300 text-indigo-600"
                            checked={
                              draftId !== null && selectedIds.includes(draftId)
                            }
                            onClick={(e) => e.stopPropagation()}
                            onChange={(e) => {
                              e.stopPropagation();
                              toggleSelection(draftId);
                            }}
                          />
                          <span className="font-bold text-gray-900 text-lg">
                            {draft.flatNumber || "N/A"}
                          </span>
                        </div>
                        <span className="px-2 py-1 text-[10px] font-bold uppercase tracking-wider bg-amber-100 text-amber-800 rounded">
                          Pending
                        </span>
                      </div>
                      <div className="pl-8 flex justify-between items-end mt-1">
                        <span className="text-sm text-gray-500">
                          {draft.transactionDate}
                        </span>
                        <span className="font-bold text-emerald-600 text-lg">
                          ₹{draft.amount.toLocaleString("en-IN")}
                        </span>
                      </div>
                    </div>
                  );
                })}
              </div>

              {/* ACTION BAR (Sticky on mobile, inline on desktop) */}
              <div className="fixed bottom-0 left-0 right-0 bg-white border-t border-gray-200 p-4 shadow-[0_-4px_6px_-1px_rgba(0,0,0,0.05)] z-20 md:relative md:bg-gray-50 md:shadow-none md:border-t flex justify-between items-center transition-all">
                <span className="text-sm text-gray-600 font-medium">
                  <span className="font-bold text-indigo-600">
                    {selectedIds.length}
                  </span>{" "}
                  selected
                </span>

                <div className="flex gap-2 md:gap-3">
                  <button
                    onClick={() => openModal("REJECT")}
                    disabled={selectedIds.length === 0}
                    className="px-4 py-2 md:px-5 md:py-2.5 bg-white hover:bg-red-50 text-red-600 border border-red-200 hover:border-red-300 rounded-lg font-medium disabled:opacity-50 disabled:cursor-not-allowed transition-all text-sm md:text-base shadow-sm">
                    Reject
                  </button>
                  <button
                    onClick={() => openModal("APPROVE")}
                    disabled={selectedIds.length === 0}
                    className="px-4 py-2 md:px-5 md:py-2.5 bg-emerald-600 hover:bg-emerald-700 text-white rounded-lg font-medium disabled:opacity-50 disabled:cursor-not-allowed transition-all text-sm md:text-base shadow-sm">
                    Approve
                  </button>
                </div>
              </div>
            </>
          )}
        </div>
      )}

      {/* CUSTOM ACTION MODAL (Replaces window.prompt/confirm) */}
      {modalConfig.isOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-md overflow-hidden animate-in fade-in zoom-in-95 duration-200">
            <div
              className={`px-6 py-4 border-b ${modalConfig.type === "APPROVE" ? "bg-emerald-50" : "bg-red-50"}`}>
              <h3
                className={`text-lg font-bold ${modalConfig.type === "APPROVE" ? "text-emerald-800" : "text-red-800"}`}>
                {modalConfig.title}
              </h3>
            </div>

            <div className="p-6">
              {errorMessage && (
                <p className="text-red-600 text-sm mb-4 bg-red-50 p-2 rounded">
                  {errorMessage}
                </p>
              )}

              {modalConfig.type === "APPROVE" ? (
                <p className="text-gray-600">
                  You are about to approve{" "}
                  <strong className="text-gray-900">
                    {selectedIds.length}
                  </strong>{" "}
                  transactions. These will be permanently recorded in the main
                  ledger. Are you sure?
                </p>
              ) : (
                <div>
                  <p className="text-gray-600 text-sm mb-3">
                    You are rejecting{" "}
                    <strong className="text-gray-900">
                      {selectedIds.length}
                    </strong>{" "}
                    transactions. Please provide a reason for the Maker.
                  </p>
                  <textarea
                    className="w-full border border-gray-300 rounded-lg p-3 text-sm focus:ring-2 focus:ring-red-500 focus:border-red-500 outline-none transition-all"
                    rows="3"
                    placeholder="e.g., Incorrect amount entered..."
                    value={rejectReason}
                    onChange={(e) =>
                      setRejectReason(e.target.value)
                    }></textarea>
                </div>
              )}
            </div>

            <div className="px-6 py-4 bg-gray-50 flex justify-end gap-3 border-t">
              <button
                onClick={closeModal}
                disabled={actionLoading}
                className="px-4 py-2 text-gray-600 bg-white border border-gray-300 hover:bg-gray-100 rounded-lg font-medium transition-colors">
                Cancel
              </button>
              <button
                onClick={
                  modalConfig.type === "APPROVE"
                    ? executeApproval
                    : executeRejection
                }
                disabled={actionLoading}
                className={`px-4 py-2 text-white rounded-lg font-medium flex items-center shadow-sm transition-colors ${
                  modalConfig.type === "APPROVE"
                    ? "bg-emerald-600 hover:bg-emerald-700"
                    : "bg-red-600 hover:bg-red-700"
                }`}>
                {actionLoading && <LoadingSpinner />}
                {modalConfig.type === "APPROVE"
                  ? "Confirm Approval"
                  : "Submit Rejection"}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default ApproveIncomePage;
