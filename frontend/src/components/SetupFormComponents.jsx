const alertStyles = {
  error: "rounded-xl bg-rose-50 p-4 text-sm text-rose-800",
  success: "rounded-xl bg-emerald-50 p-4 text-sm text-emerald-800",
  info: "rounded-xl border border-blue-200 bg-blue-50 p-4 text-sm text-blue-900",
};

const fieldClassName = "block space-y-1.5 text-sm font-medium text-slate-700";

export const SetupPageHeader = ({ eyebrow, title, description }) => (
  <header className="border-b border-slate-200 pb-5">
    <p className="text-xs font-semibold uppercase tracking-wider text-slate-500">
      {eyebrow}
    </p>
    <h1 className="mt-1 text-2xl font-bold text-slate-900">{title}</h1>
    <p className="mt-1 text-sm text-slate-600">{description}</p>
  </header>
);

export const SetupStatusMessage = ({
  type = "error",
  children,
  className = "",
}) => (
  <div
    role={type === "success" ? "status" : "alert"}
    className={`${alertStyles[type]} ${className}`}>
    {children}
  </div>
);

export const SetupFormField = ({
  label,
  children,
  className = "",
}) => (
  <label className={`${fieldClassName} ${className}`}>
    {label}
    {children}
  </label>
);

export const SetupSubmitButton = ({
  isSubmitting,
  submittingLabel = "Saving...",
  disabled = false,
  children,
  className = "",
}) => (
  <button
    type="submit"
    disabled={disabled || isSubmitting}
    className={`rounded-xl bg-blue-600 px-5 py-3 text-sm font-semibold text-white hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-60 ${className}`}>
    {isSubmitting ? submittingLabel : children}
  </button>
);
