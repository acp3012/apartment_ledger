import { useEffect, useState } from "react";
import { masterDataService } from "../api/masterDataService";
import { useAuth } from "../context/AuthContext";

const MasterEntriesPage = () => {
  const { user } = useAuth();
  const [expenseCategories, setExpenseCategories] = useState([]);
  const [incomeCategories, setIncomeCategories] = useState([]);
  const [paymentModes, setPaymentModes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState("");

  useEffect(() => {
    let isMounted = true;

    const loadMasterEntries = async () => {
      try {
        const [expenses, incomes, modes] = await Promise.all([
          masterDataService.getExpenseCategories(),
          masterDataService.getIncomeCategories(),
          masterDataService.getPaymentModes(),
        ]);

        if (isMounted) {
          setExpenseCategories(expenses);
          setIncomeCategories(incomes);
          setPaymentModes(modes);
        }
      } catch (error) {
        if (isMounted) {
          setErrorMessage(
            error.response?.data?.message || "Failed to load master entries.",
          );
        }
      } finally {
        if (isMounted) setLoading(false);
      }
    };

    loadMasterEntries();
    return () => {
      isMounted = false;
    };
  }, []);

  const lists = [
    { title: "Income Categories", entries: incomeCategories, kind: "category" },
    {
      title: "Expense Categories",
      entries: expenseCategories,
      kind: "category",
    },
    { title: "Payment Modes", entries: paymentModes, kind: "payment" },
  ];

  return (
    <div className="mx-auto max-w-7xl space-y-6">
      <header className="border-b border-slate-200 pb-5">
        <p className="text-xs font-semibold uppercase tracking-wider text-slate-500">
          Administration
        </p>
        <h1 className="mt-1 text-2xl font-bold text-slate-900">
          Master Entries
        </h1>
        <p className="mt-1 text-sm text-slate-600">
          {user?.apartmentName || "Apartment"}
        </p>
      </header>

      {errorMessage && (
        <div
          role="alert"
          className="border-l-4 border-rose-500 bg-rose-50 p-4 text-sm text-rose-800">
          {errorMessage}
        </div>
      )}

      {loading ? (
        <p className="py-8 text-sm text-slate-600">Loading master entries...</p>
      ) : (
        <div className="grid gap-8 lg:grid-cols-2">
          {lists.map(({ title, entries, kind }) => (
            <section
              key={title}
              className="min-w-0">
              <div className="mb-3 flex items-center justify-between border-b border-slate-200 pb-2">
                <h2 className="text-base font-semibold text-slate-900">
                  {title}
                </h2>
                <span className="text-xs text-slate-500">
                  {entries.length} entries
                </span>
              </div>
              {entries.length === 0 ? (
                <p className="py-4 text-sm text-slate-500">No entries found.</p>
              ) : (
                <div className="overflow-x-auto">
                  <table className="w-full border-collapse text-left text-sm">
                    <thead>
                      <tr className="border-b border-slate-200 text-xs uppercase text-slate-500">
                        <th className="py-2 pr-4 font-medium">Code / ID</th>
                        <th className="py-2 pr-4 font-medium">Name</th>
                        {kind === "category" && (
                          <th className="py-2 font-medium">Type</th>
                        )}
                      </tr>
                    </thead>
                    <tbody>
                      {entries.map((entry, index) => (
                        <tr
                          key={
                            entry.id ??
                            entry.code ??
                            entry.categoryCode ??
                            index
                          }
                          className="border-b border-slate-100 last:border-0">
                          <td className="py-3 pr-4 text-slate-600">
                            {entry.code ??
                              entry.categoryCode ??
                              entry.ledgerCategoryCode ??
                              entry.id ??
                              "-"}
                          </td>
                          <td className="py-3 pr-4 font-medium text-slate-800">
                            {kind === "category"
                              ? (entry.categoryName ?? "-")
                              : (entry.name ?? entry.paymentModeName ?? "-")}
                          </td>
                          {kind === "category" && (
                            <td className="py-3 text-slate-600">
                              {entry.isFlatMaintenance
                                ? "Maintenance"
                                : "General"}
                            </td>
                          )}
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </section>
          ))}
        </div>
      )}
    </div>
  );
};

export default MasterEntriesPage;
