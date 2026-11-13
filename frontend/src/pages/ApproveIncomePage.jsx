import React, { useState } from "react";
import { useParams } from "react-router-dom";
import { toast } from "react-toastify";
import { incomeService } from "../api/incomeService";
import { useAuth } from "../context/AuthContext";
import { usePeriod } from "../context/PeriodContext";
import ConfirmationModal from "../components/ConfirmationModal";

const ApproveIncomePage = ({ apartmentId: routeApartmentId }) => {
  const { apartmentId: routeParamId } = useParams();
  const { user } = useAuth();
  const { activePeriod } = usePeriod();

  const apartmentId = Number(
    routeApartmentId ?? routeParamId ?? user?.apartmentId ?? user?.apartment_id,
  );

  // 2. Core State
  const [drafts, setDrafts] = useState([]);
  const [selectedIds, setSelectedIds] = useState([]);

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

    return true;
  };

  const handleLoadPending = async () => {
    if (!hasValidSession()) return;

    try {
      setLoading(true);
      setErrorMessage("");

      if (!activePeriod) {
        throw new Error("Active ledger period is not available.");
      }

      const pendingDrafts = await incomeService.getPendingDrafts(
        apartmentId,
        activePeriod.year,
        activePeriod.month,
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
      await incomeService.approveDrafts(apartmentId, selectedIds);

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
      await incomeService.rejectDrafts(apartmentId, selectedIds, rejectReason);

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
          {activePeriod && (
            <div className="bg-slate-50 px-4 md:px-6 py-3 border-b border-gray-200 flex justify-end items-center">
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
              {/* RESPONSIVE TABLE FOR DESKTOP & MOBILE */}
              <div className="overflow-x-auto">
                <table className="w-full min-w-[500px] text-left border-collapse">
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
                      <th className="p-4 text-sm font-semibold text-gray-600 uppercase tracking-wider whitespace-nowrap">
                        Date
                      </th>
                      <th className="p-4 text-sm font-semibold text-gray-600 uppercase tracking-wider whitespace-nowrap">
                        Flat/Unit
                      </th>
                      <th className="p-4 text-sm font-semibold text-gray-600 uppercase tracking-wider whitespace-nowrap">
                        Amount
                      </th>
                      <th className="p-4 text-sm font-semibold text-gray-600 uppercase tracking-wider whitespace-nowrap">
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
                          <td className="p-4 text-gray-600 whitespace-nowrap">
                            {draft.transactionDate}
                          </td>
                          <td className="p-4 font-medium text-gray-900 whitespace-nowrap">
                            {draft.flatNumber || "N/A"}
                          </td>
                          <td className="p-4 font-semibold text-emerald-600 whitespace-nowrap">
                            ₹{draft.amount.toLocaleString("en-IN")}
                          </td>
                          <td className="p-4 whitespace-nowrap">
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
          modalConfig.type === "APPROVE" ? executeApproval : executeRejection
        }>
        {modalConfig.type === "APPROVE" ? (
          <p className="text-gray-600">
            You are about to approve{" "}
            <strong className="text-gray-900">{selectedIds.length}</strong>{" "}
            transactions. These will be permanently recorded in the main ledger.
            Are you sure?
          </p>
        ) : (
          <div>
            <p className="mb-3 text-sm text-gray-600">
              You are rejecting{" "}
              <strong className="text-gray-900">{selectedIds.length}</strong>{" "}
              transactions. Please provide a reason for the Maker.
            </p>
            <textarea
              className="w-full rounded-lg border border-gray-300 p-3 text-sm outline-none transition-all focus:border-red-500 focus:ring-2 focus:ring-red-500"
              rows="3"
              placeholder="e.g., Incorrect amount entered..."
              value={rejectReason}
              onChange={(e) => setRejectReason(e.target.value)}
            />
          </div>
        )}
      </ConfirmationModal>
    </div>
  );
};

export default ApproveIncomePage;
