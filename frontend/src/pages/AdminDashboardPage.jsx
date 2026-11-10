import { Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import PeriodManagementComponent from "../components/PeriodManagementComponent";

const setupActions = [
  {
    title: "One-Time Setup",
    description:
      "Review the setup steps required before the apartment ledger is ready.",
    path: "/admin/setup",
    icon: "🧰",
  },
  {
    title: "Create Flats",
    description:
      "Add apartment flats and their owner contact details.",
    path: "/admin/setup/flats",
    icon: "🏢",
  },
  {
    title: "Financial Starting Period",
    description:
      "Choose the month and year from which transactions will be recorded.",
    path: "/admin/setup/period",
    icon: "📅",
  },
  {
    title: "Opening Balance",
    description:
      "Carry forward existing financial balances, if the apartment has prior records.",
    path: "/admin/opening-balance",
    icon: "＋",
  },
];

const managementActions = [
  {
    title: "Master Data Management",
    description:
      "Review income categories, expense categories, and payment modes.",
    path: "/admin/master-entries",
    icon: "⚙️",
  },
  {
    title: "Maintenance Fees",
    description: "Manage apartment maintenance fee schedules.",
    path: "/admin/maintenance-fees",
    icon: "💰",
  },
  {
    title: "Ledger Entries",
    description: "Record and manage apartment income and expenses.",
    path: "/dashboard/expense",
    icon: "💸",
  },
  {
    title: "Monthly Reports",
    description: "View apartment-wide income, expenses, and statements.",
    path: "/monthly-statement",
    icon: "📊",
  },
];

const flatsAction = {
  title: "Flats",
  description: "Review and update registered flat and owner details.",
  path: "/admin/setup/flats",
  icon: "🏢",
};

const AdminDashboardPage = () => {
  const { user } = useAuth();
  const isSetupCompleted = user?.isSetupCompleted === true;
  const actions = isSetupCompleted
    ? [...managementActions, flatsAction]
    : [...setupActions, ...managementActions];

  return (
    <div className="mx-auto max-w-7xl space-y-6 p-6">
      <header className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
        <p className="text-xs font-semibold uppercase tracking-wider text-blue-600">
          Administration
        </p>
        <h1 className="mt-1 text-2xl font-bold text-slate-900">
          Welcome, {user?.displayName || "Administrator"}
        </h1>
        <p className="mt-1 text-sm text-slate-600">
          {user?.apartmentName || "Apartment"} · Admin dashboard
        </p>
        <p
          className={`mt-4 inline-flex rounded-full px-3 py-1 text-xs font-semibold ${
            isSetupCompleted
              ? "bg-emerald-50 text-emerald-700"
              : "bg-amber-50 text-amber-800"
          }`}>
          {isSetupCompleted ? "Setup complete" : "Initial setup in progress"}
        </p>
      </header>

      {!isSetupCompleted && (
        <section className="rounded-2xl border border-amber-200 bg-amber-50 p-5">
          <h2 className="font-semibold text-amber-950">
            Complete apartment setup
          </h2>
          <p className="mt-1 text-sm text-amber-900">
            Configure the apartment before starting to use the ledger. Personal
            flat payment information is not shown on the admin dashboard.
          </p>
        </section>
      )}

      <section>
        <h2 className="mb-3 text-sm font-semibold uppercase tracking-wider text-slate-500">
          {isSetupCompleted ? "Management" : "Setup and management"}
        </h2>
        <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
          {actions.map((action) => (
            <Link
              key={action.path}
              to={action.path}
              className="group rounded-2xl border border-slate-200 bg-white p-5 shadow-sm transition hover:-translate-y-0.5 hover:border-blue-300 hover:shadow-md">
              <span className="flex h-11 w-11 items-center justify-center rounded-xl bg-blue-50 text-xl">
                {action.icon}
              </span>
              <h3 className="mt-4 font-bold text-slate-900 group-hover:text-blue-700">
                {action.title}
              </h3>
              <p className="mt-1 text-sm leading-6 text-slate-600">
                {action.description}
              </p>
            </Link>
          ))}
        </div>
      </section>

      {isSetupCompleted && (
        <section>
          <PeriodManagementComponent />
        </section>
      )}
    </div>
  );
};

export default AdminDashboardPage;
