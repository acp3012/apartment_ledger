const variantStyles = {
  primary: {
    header: "border-indigo-100 bg-indigo-50",
    title: "text-indigo-900",
    confirm: "bg-indigo-600 hover:bg-indigo-700",
  },
  success: {
    header: "border-emerald-100 bg-emerald-50",
    title: "text-emerald-800",
    confirm: "bg-emerald-600 hover:bg-emerald-700",
  },
  danger: {
    header: "border-red-100 bg-red-50",
    title: "text-red-800",
    confirm: "bg-red-600 hover:bg-red-700",
  },
};

const ConfirmationModal = ({
  isOpen,
  title,
  variant = "primary",
  errorMessage,
  children,
  cancelLabel = "Cancel",
  confirmLabel = "Confirm",
  loadingLabel = "Working...",
  isLoading = false,
  confirmDisabled = false,
  onCancel,
  onConfirm,
}) => {
  if (!isOpen) return null;

  const styles = variantStyles[variant] ?? variantStyles.primary;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4 backdrop-blur-sm">
      <div
        role="dialog"
        aria-modal="true"
        aria-labelledby="confirmation-modal-title"
        className="w-full max-w-md overflow-hidden rounded-xl bg-white shadow-xl animate-in fade-in zoom-in-95 duration-200">
        <div className={`border-b px-6 py-4 ${styles.header}`}>
          <h3
            id="confirmation-modal-title"
            className={`text-lg font-bold ${styles.title}`}>
            {title}
          </h3>
        </div>

        <div className="p-6">
          {errorMessage && (
            <p className="mb-4 rounded bg-red-50 p-3 text-sm text-red-700">
              {errorMessage}
            </p>
          )}
          {children}
        </div>

        <div className="flex justify-end gap-3 border-t bg-gray-50 px-6 py-4">
          <button
            type="button"
            onClick={onCancel}
            disabled={isLoading}
            className="rounded-lg border border-gray-300 bg-white px-4 py-2 font-medium text-gray-600 transition-colors hover:bg-gray-100 disabled:opacity-50">
            {cancelLabel}
          </button>
          <button
            type="button"
            onClick={onConfirm}
            disabled={isLoading || confirmDisabled}
            className={`rounded-lg px-4 py-2 font-medium text-white shadow-sm transition-colors disabled:cursor-not-allowed disabled:opacity-50 ${styles.confirm}`}>
            {isLoading ? loadingLabel : confirmLabel}
          </button>
        </div>
      </div>
    </div>
  );
};

export default ConfirmationModal;
