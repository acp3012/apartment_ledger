import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { ledgerService } from "../api/ledgerService";
import { useAuth } from "../context/AuthContext";

const DashboardPage = ({ apartmentId: routeApartmentId, flatId: routeFlatId }) => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const apartmentId = Number(routeApartmentId ?? user?.apartmentId ?? 1);
  const flatId = Number(routeFlatId ?? user?.flatId ?? 5);

  const currentDate = new Date();
  const [year, setYear] = useState(currentDate.getFullYear());
  const [month, setMonth] = useState(currentDate.getMonth() + 1);

  const [paymentStatus, setPaymentStatus] = useState(null);
  const [notices, setNotices] = useState([]);
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  const fetchData = async () => {
    try {
      setLoading(true);
      setErrorMessage("");

      const [statusData, noticeData] = await Promise.all([
        ledgerService.getFlatPaymentStatus(apartmentId, flatId, year, month),
        ledgerService.getRecentNotices ? ledgerService.getRecentNotices(apartmentId) : Promise.resolve([
          {
            id: 1,
            title: "Annual General Body Meeting",
            message: "AGM is scheduled for Sunday at 10:00 AM in the clubhouse. All owners are requested to attend.",
            category: "MEETING",
            postedDate: "2026-08-05",
            postedBy: "Association President"
          },
          {
            id: 2,
            title: "Water Supply Interruption",
            message: "Municipal water supply will be shut down for maintenance on Thursday between 10 AM and 2 PM.",
            category: "NOTICE",
            postedDate: "2026-08-03",
            postedBy: "Maintenance Admin"
          }
        ])
      ]);

      setPaymentStatus(statusData);
      setNotices(noticeData);
    } catch (error) {
      console.error("Error fetching dashboard data:", error);
      setErrorMessage("Failed to load dashboard details.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [apartmentId, flatId, year, month]);

  const isPaid = paymentStatus?.status === "PAID";

  return (
    <div className="p-6 max-w-7xl mx-auto space-y-6">
      {/* HEADER & PERIOD SELECTOR */}
      <div className="flex flex-col md:flex-row justify-between items-start md:items-center bg-white p-5 rounded-2xl shadow-sm border border-slate-200 gap-4">
        <div>
          <p className="text-xs font-semibold uppercase tracking-wider text-slate-400">Resident Portal</p>
          <h2 className="text-xl md:text-2xl font-bold text-slate-800">
            Welcome, {paymentStatus?.ownerName || user?.name || "Resident"} ({paymentStatus?.flatNumber || "Flat"})
          </h2>
        </div>

        <div className="flex items-center gap-3">
          <select
            value={month}
            onChange={(e) => setMonth(Number(e.target.value))}
            className="border border-slate-300 rounded-xl px-3 py-2 text-sm bg-white outline-none focus:ring-2 focus:ring-blue-500">
            {[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12].map((m) => (
              <option key={m} value={m}>
                {new Date(0, m - 1).toLocaleString("en", { month: "long" })}
              </option>
            ))}
          </select>
          <input
            type="number"
            value={year}
            onChange={(e) => setYear(Number(e.target.value))}
            className="w-24 border border-slate-300 rounded-xl px-3 py-2 text-sm bg-white outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
      </div>

      {errorMessage && (
        <div className="p-4 bg-red-50 border-l-4 border-red-500 text-red-700 rounded-r-xl text-sm font-medium">
          {errorMessage}
        </div>
      )}

      {/* DASHBOARD GRID */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        
        {/* LEFT COLUMN: PERSONAL PAYMENT STATUS & MONTHLY REPORT LINK */}
        <div className="lg:col-span-2 space-y-6">
          
          {/* CARD 1: REAL CURRENT MAINTENANCE / PAYMENT STATUS */}
          <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-6 flex flex-col justify-between">
          <div>
    <div className="flex justify-between items-start">
      <div>
        <p className="text-xs font-semibold uppercase tracking-wider text-slate-400">Current Maintenance Period</p>
        <h3 className="text-lg font-bold text-slate-800 mt-0.5">
          {new Date(0, month - 1).toLocaleString("en", { month: "long" })} {year}
        </h3>
      </div>
      <span className={`px-4 py-1.5 rounded-full text-xs font-bold tracking-wide uppercase shadow-xs ${
        isPaid ? "bg-emerald-100 text-emerald-800 border border-emerald-200" : "bg-amber-100 text-amber-800 border border-amber-200"
      }`}>
        {loading ? "LOADING..." : (paymentStatus?.status || "PENDING")}
      </span>
    </div>

           {loading ? (
      <div className="py-12 text-center text-slate-400 text-sm">Checking status...</div>
    ) : (
      <div className="mt-6 grid grid-cols-1 sm:grid-cols-2 gap-4">
        <div className="bg-slate-50 p-4 rounded-xl border border-slate-100">
          <p className="text-xs text-slate-500 font-medium">Amount {isPaid ? "Paid" : "Due"}</p>
          <p className={`text-2xl font-bold mt-1 ${isPaid ? "text-emerald-700" : "text-amber-700"}`}>
            ₹{Number(isPaid ? (paymentStatus?.amountPaid ?? 0) : (paymentStatus?.arrearsAmount ?? 0)).toLocaleString("en-IN")}
          </p>
        </div>

        <div className="bg-slate-50 p-4 rounded-xl border border-slate-100">
          <p className="text-xs text-slate-500 font-medium">Payment Record</p>
          <p className="text-sm font-semibold text-slate-800 mt-1">
            {isPaid ? `${paymentStatus?.paymentModeName || ""} - ${paymentStatus?.referenceNumber || ""}` : "Pending Payment"}
          </p>
          {isPaid && paymentStatus?.lastPaymentDate && (
            <p className="text-xs text-slate-400 mt-0.5">Paid on: {paymentStatus.lastPaymentDate}</p>
          )}
        </div>
      </div>
    )}
  </div>

  <div className="mt-6 pt-4 border-t border-slate-100 flex justify-between items-center text-xs text-slate-400">
    <span>Flat: <strong className="text-slate-600">{paymentStatus?.flatNumber || "---"}</strong></span>
    <span>Owner: <strong className="text-slate-600">{paymentStatus?.ownerName || "---"}</strong></span>
  </div>
</div>

          {/* CARD 2: LINK TO MONTHLY REPORT PAGE */}
          <div className="bg-gradient-to-r from-blue-600 to-indigo-700 rounded-2xl shadow-sm p-6 text-white flex justify-between items-center">
            <div>
              <p className="text-xs uppercase tracking-wider text-blue-200 font-semibold">Community Books</p>
              <h3 className="text-lg font-bold mt-1">Monthly Statement & Reports</h3>
              <p className="text-xs text-blue-100 mt-0.5">View complete apartment income, expenses, and category charts.</p>
            </div>
            <button
              onClick={() => navigate("/monthly-statement")}
              className="bg-white text-blue-700 hover:bg-blue-50 px-5 py-2.5 rounded-xl text-sm font-bold shadow-sm transition-all whitespace-nowrap ml-4">
              Open Report &rarr;
            </button>
          </div>

        </div>

        {/* RIGHT COLUMN: NOTICE BOARD */}
        <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-6 flex flex-col">
          <div className="flex justify-between items-center mb-4">
            <h3 className="font-bold text-slate-800 text-sm uppercase tracking-wider">Notice Board</h3>
            <span className="text-xs bg-blue-50 text-blue-600 px-2 py-0.5 rounded-md font-medium">Updates</span>
          </div>

          <div className="space-y-3 overflow-y-auto max-h-[380px] pr-1">
            {notices.length === 0 ? (
              <p className="text-slate-400 text-sm text-center py-8">No notices available.</p>
            ) : (
              notices.map((notice) => (
                <div key={notice.id} className="p-3 bg-slate-50 rounded-xl border border-slate-100 hover:bg-slate-100/60 transition-colors">
                  <div className="flex justify-between items-start">
                    <h4 className="text-xs font-bold text-slate-800">{notice.title}</h4>
                    <span className="text-[10px] text-slate-400">{notice.postedDate}</span>
                  </div>
                  <p className="text-xs text-slate-600 mt-1 leading-relaxed">{notice.message}</p>
                  <p className="text-[10px] text-blue-600 font-medium mt-2">— {notice.postedBy}</p>
                </div>
              ))
            )}
          </div>
        </div>

      </div>
    </div>
  );
};

export default DashboardPage;
