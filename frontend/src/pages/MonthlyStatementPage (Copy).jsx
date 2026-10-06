import React, { useState, useRef, useEffect } from "react";
import { ledgerService } from "../api/ledgerService";
import LedgerPeriodBanner from "../components/LedgerPeriodBanner";
import { useAuth } from "../context/AuthContext";
import { ResponsiveContainer, PieChart, Pie, Cell, BarChart, Bar, XAxis, YAxis, Tooltip, Legend, CartesianGrid } from "recharts";

const COLORS = ["#059669", "#3b82f6", "#d97706", "#8b5cf6", "#ec4899", "#14b8a6"];

const MonthlyStatement = ({ apartmentId: routeApartmentId }) => {
  const { user } = useAuth();
  const apartmentId = Number(routeApartmentId ?? user?.apartmentId ?? 1);

  const currentDate = new Date();
  const [year, setYear] = useState(currentDate.getFullYear());
  const [month, setMonth] = useState(currentDate.getMonth() + 1);

  const [summary, setSummary] = useState(null);
  const [details, setDetails] = useState([]);
  const [paymentModes, setPaymentModes] = useState([]);
  const [expenseCategories, setExpenseCategories] = useState([]);
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const [hasSearched, setHasSearched] = useState(false);

  const reportRef = useRef();

  const fetchLedgerData = async () => {
    try {
      setLoading(true);
      setErrorMessage("");

      const [summaryData, detailsData, paymentModeData, expenseCategoryData] = await Promise.all([
        ledgerService.getLedgerSummary(apartmentId, year, month),
        ledgerService.getLedgerDetails(apartmentId, year, month),
        ledgerService.getPaymentModeSummary(apartmentId, year, month),
        ledgerService.getExpenseCategorySummary(apartmentId, year, month),
      ]);

      setSummary(summaryData);
      setDetails(detailsData);
      setPaymentModes(paymentModeData);
      setExpenseCategories(expenseCategoryData);
      setHasSearched(true);
    } catch (error) {
      console.error("Error fetching monthly ledger data:", error);
      setErrorMessage("Failed to load monthly statement report.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchLedgerData();
  }, [apartmentId]);

  const handleDownloadPDF = () => {
    window.print();
  };

  const totalIncomeSum = details.reduce((acc, curr) => acc + (Number(curr.incomeAmount) || 0), 0);
  const totalExpenseSum = details.reduce((acc, curr) => acc + (Number(curr.expenseAmount) || 0), 0);

  return (
    <div className="p-6 max-w-7xl mx-auto">
      {/* HEADER & CONTROLS */}
      <div className="print:hidden flex flex-col md:flex-row justify-between items-start md:items-center bg-white p-5 rounded-2xl shadow-sm border border-slate-200 mb-6 gap-4">
        <div>
          <p className="text-xs font-semibold uppercase tracking-wider text-slate-400">Financial Reports</p>
          <h2 className="text-xl md:text-2xl font-bold text-slate-800">Monthly Statement</h2>
        </div>

        <div className="flex flex-wrap items-center gap-3">
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
          <button
            onClick={fetchLedgerData}
            disabled={loading}
            className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-xl text-sm font-medium transition-all shadow-xs disabled:opacity-50">
            {loading ? "Loading..." : "Filter"}
          </button>
          
          {hasSearched && details.length > 0 && (
            <button
              onClick={handleDownloadPDF}
              className="bg-slate-800 hover:bg-slate-900 text-white px-4 py-2 rounded-xl text-sm font-medium transition-all shadow-xs flex items-center gap-2">
              <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
              </svg>
              Export PDF
            </button>
          )}
        </div>
      </div>

      {/* REPORT CONTENT AREA */}
      <div ref={reportRef} className="bg-slate-50/50 p-2 rounded-2xl space-y-6">
        <LedgerPeriodBanner apartmentId={apartmentId} />

        {errorMessage && (
          <div className="p-4 bg-red-50 border-l-4 border-red-500 text-red-700 rounded-r-xl text-sm font-medium">
            {errorMessage}
          </div>
        )}

        {/* SUMMARY KPI CARDS */}
        {summary && (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
            <div className="bg-white p-4 rounded-2xl border border-slate-200 shadow-sm">
              <p className="text-xs font-semibold uppercase tracking-wider text-slate-400">Opening Balance</p>
              <p className="text-xl font-bold text-slate-800 mt-1">₹{summary.openingBalance.toLocaleString("en-IN")}</p>
            </div>
            <div className="bg-white p-4 rounded-2xl border border-slate-200 shadow-sm">
              <p className="text-xs font-semibold uppercase tracking-wider text-emerald-600">Total Income</p>
              <p className="text-xl font-bold text-emerald-700 mt-1">₹{summary.totalIncome.toLocaleString("en-IN")}</p>
            </div>
            <div className="bg-white p-4 rounded-2xl border border-slate-200 shadow-sm">
              <p className="text-xs font-semibold uppercase tracking-wider text-rose-600">Total Expense</p>
              <p className="text-xl font-bold text-rose-600 mt-1">₹{summary.totalExpense.toLocaleString("en-IN")}</p>
            </div>
            <div className="bg-white p-4 rounded-2xl border border-slate-200 shadow-sm">
              <p className="text-xs font-semibold uppercase tracking-wider text-blue-600">Closing Balance</p>
              <p className={`text-xl font-bold mt-1 ${summary.closingBalance >= 0 ? "text-slate-800" : "text-rose-600"}`}>
                ₹{summary.closingBalance.toLocaleString("en-IN")}
              </p>
            </div>
          </div>
        )}

        {/* ANALYTICS CHARTS SECTION (PIE + BAR CHART) */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* 1. Payment Mode Income Pie Chart */}
          <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-5 flex flex-col justify-between">
            <h3 className="font-bold text-slate-800 text-sm uppercase tracking-wider mb-2">Income by Payment Mode</h3>
            <div className="h-72 w-full">
              {paymentModes.length > 0 ? (
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart>
                    <Pie
                      data={paymentModes}
                      dataKey="incomeAmount"
                      nameKey="paymentModeName"
                      cx="50%"
                      cy="50%"
                      outerRadius={80}
                      innerRadius={35}
                      paddingAngle={4}
                      label={({ name, percent }) => `${name}: ${(percent * 100).toFixed(0)}%`}>
                      {paymentModes.map((entry, index) => (
                        <Cell key={`cell-pm-${index}`} fill={COLORS[index % COLORS.length]} />
                      ))}
                    </Pie>
                    <Tooltip 
                      formatter={(value, name, props) => [
                        `₹${Number(value).toLocaleString("en-IN")} (${props.payload.transactionCount} txn)`, 
                        name
                      ]}
                      contentStyle={{ backgroundColor: "#ffffff", borderRadius: "12px", border: "1px solid #e2e8f0", boxShadow: "0 4px 6px -1px rgb(0 0 0 / 0.1)" }}
                    />
                    <Legend verticalAlign="bottom" height={36} iconType="circle" />
                  </PieChart>
                </ResponsiveContainer>
              ) : (
                <div className="h-full flex items-center justify-center text-slate-400 text-sm">No income data available</div>
              )}
            </div>
          </div>

          {/* 2. Expense Category Bar Chart */}
          <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-5 flex flex-col justify-between">
            <h3 className="font-bold text-slate-800 text-sm uppercase tracking-wider mb-2">Category-Wise Expenses</h3>
            <div className="h-72 w-full">
              {expenseCategories.length > 0 ? (
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={expenseCategories} margin={{ top: 15, right: 10, left: 0, bottom: 25 }}>
                    <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
                    <XAxis 
                      dataKey="ledgerCategoryName" 
                      tick={{ fontSize: 10, fill: "#64748b" }} 
                      interval={0}
                      angle={-15}
                      textAnchor="end"
                      axisLine={{ stroke: "#cbd5e1" }} 
                    />
                    <YAxis tick={{ fontSize: 11, fill: "#64748b" }} axisLine={{ stroke: "#cbd5e1" }} />
                    <Tooltip 
                      formatter={(value, name, props) => [
                        `₹${Number(value).toLocaleString("en-IN")} (${props.payload.transactionCount} txn)`, 
                        "Amount"
                      ]}
                      contentStyle={{ backgroundColor: "#ffffff", borderRadius: "12px", border: "1px solid #e2e8f0", boxShadow: "0 4px 6px -1px rgb(0 0 0 / 0.1)" }}
                    />
                    <Bar dataKey="expenseAmount" fill="#e11d48" radius={[6, 6, 0, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              ) : (
                <div className="h-full flex items-center justify-center text-slate-400 text-sm">No expense data available</div>
              )}
            </div>
          </div>
        </div>

        {/* DETAIL LEDGER TABLE */}
        <div className="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden">
          <div className="px-6 py-4 border-b border-slate-200 bg-slate-50 flex justify-between items-center">
            <div>
              <h3 className="font-bold text-slate-800 text-base">Transactions</h3>
              <p className="text-xs text-slate-500 mt-0.5">{details.length} entries recorded</p>
            </div>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead className="bg-slate-50 border-b border-slate-200 text-slate-500 uppercase text-xs tracking-wider">
                <tr>
                  <th className="p-4">Date</th>
                  <th className="p-4">Category</th>
                  <th className="p-4">Remarks / Ref</th>
                  <th className="p-4">Payment Mode</th>
                  <th className="p-4 text-right">Income (₹)</th>
                  <th className="p-4 text-right">Expense (₹)</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 text-sm">
                {details.length === 0 ? (
                  <tr>
                    <td colSpan="6" className="p-12 text-center text-slate-400">
                      Select a period and click Filter to view transactions.
                    </td>
                  </tr>
                ) : (
                  details.map((item) => {
                    const displayCategory = item.isFlatMaintenance && item.flatNumber
                      ? `${item.categoryName} (${item.flatNumber})`
                      : item.categoryName;

                    return (
                      <tr key={item.id} className="hover:bg-slate-50/60 transition-colors">
                        <td className="p-4 text-slate-600 whitespace-nowrap">{item.transactionDate}</td>
                        <td className="p-4 font-semibold text-slate-800">
                          {displayCategory}
                          {item.ownerName && <span className="block text-xs font-normal text-slate-400">{item.ownerName}</span>}
                        </td>
                        <td className="p-4 text-slate-600">
                          <p>{item.remarks || "-"}</p>
                          {item.referenceNumber && <span className="text-xs text-slate-400 font-mono">Ref: {item.referenceNumber}</span>}
                        </td>
                        <td className="p-4 text-slate-600 whitespace-nowrap">{item.paymentModeName}</td>
                        <td className="p-4 text-right font-medium text-emerald-600">
                          {item.incomeAmount != null ? `+₹${Number(item.incomeAmount).toLocaleString("en-IN")}` : "-"}
                        </td>
                        <td className="p-4 text-right font-medium text-rose-600">
                          {item.expenseAmount != null ? `-₹${Number(item.expenseAmount).toLocaleString("en-IN")}` : "-"}
                        </td>
                      </tr>
                    );
                  })
                )}
              </tbody>

              {/* TOTALS FOOTER ROW */}
              {details.length > 0 && (
                <tfoot className="bg-slate-50 border-t-2 border-slate-200 font-bold text-slate-900 text-sm">
                  <tr>
                    <td colSpan="4" className="p-4 text-right uppercase tracking-wider text-xs text-slate-500">
                      Total for Period:
                    </td>
                    <td className="p-4 text-right text-emerald-700">
                      +₹{totalIncomeSum.toLocaleString("en-IN")}
                    </td>
                    <td className="p-4 text-right text-rose-600">
                      -₹{totalExpenseSum.toLocaleString("en-IN")}
                    </td>
                  </tr>
                </tfoot>
              )}
            </table>
          </div>
        </div>
      </div>
    </div>
  );
};

export default MonthlyStatement;
