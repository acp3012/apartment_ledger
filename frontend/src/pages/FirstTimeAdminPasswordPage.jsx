import { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import api from "../api/axiosConfig";
import { useAuth } from "../context/AuthContext";

const FirstTimeAdminPasswordPage = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useAuth();
  const { emailId } = location.state ?? {};
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  if (!emailId) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-slate-50 p-4 font-sans">
        <div className="w-full max-w-md rounded-2xl border border-slate-200 bg-white p-8 text-center shadow-sm">
          <h1 className="text-2xl font-bold text-slate-900">
            Verification required
          </h1>
          <p className="mt-3 text-sm text-slate-600">
            Verify your administrator email before setting a password.
          </p>
          <Link
            to="/first-time-admin"
            className="mt-6 inline-block font-medium text-blue-600 hover:text-blue-700 hover:underline">
            Start verification
          </Link>
        </div>
      </div>
    );
  }

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError("");

    if (newPassword.length < 6) {
      setError("Password length must be at least 6 characters.");
      return;
    }
    if (newPassword !== confirmPassword) {
      setError("The passwords do not match.");
      return;
    }

    setIsLoading(true);
    try {
      await api.post("/auth/admin-password", {
        emailId,
        newPassword,
      });
    } catch (requestError) {
      setError(
        requestError.response?.data?.message ||
          "Unable to save your password. Please try again.",
      );
      setIsLoading(false);
      return;
    }

    try {
      const response = await api.post("/auth/login", {
        email: emailId,
        password: newPassword,
      });
      login(response.data);
      navigate("/admin/setup", { replace: true });
    } catch {
      setError(
        "Your password was saved, but we could not start your session. Please log in with your new password.",
      );
      setIsLoading(false);
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-[radial-gradient(circle_at_top,_#f8fbff,_#f4f7fb_26%,_#edf2f9_100%)] p-4 font-sans">
      <div className="w-full max-w-md rounded-[28px] border border-slate-200 bg-white/90 p-8 shadow-[0_24px_60px_rgba(15,23,42,0.08)] backdrop-blur-sm">
        <div className="mb-8 text-center">
          <div className="mx-auto mb-4 flex h-14 w-14 items-center justify-center rounded-2xl bg-gradient-to-br from-blue-600 to-indigo-600 text-lg font-bold text-white shadow-lg shadow-blue-500/20">
            L
          </div>
          <h1 className="text-3xl font-bold tracking-tight text-slate-900">
            Set your password
          </h1>
          <p className="mt-2 text-sm text-slate-500">
            Create a password for {emailId}.
          </p>
        </div>

        {error && (
          <div
            role="alert"
            className="mb-6 rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700">
            {error}
            {error.startsWith("Your password was saved") && (
              <div className="mt-2">
                <Link
                  to="/login"
                  className="font-semibold underline hover:no-underline">
                  Go to login
                </Link>
              </div>
            )}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-5">
          <div>
            <label
              htmlFor="new-password"
              className="mb-1.5 block text-sm font-medium text-slate-700">
              New Password
            </label>
            <input
              id="new-password"
              type="password"
              autoComplete="new-password"
              minLength={6}
              value={newPassword}
              onChange={(event) => setNewPassword(event.target.value)}
              required
              className="w-full rounded-xl border border-slate-300 bg-slate-50 px-4 py-2.5 text-slate-800 outline-none transition-all duration-200 focus:border-blue-500 focus:bg-white focus:ring-4 focus:ring-blue-100"
            />
          </div>

          <div>
            <label
              htmlFor="confirm-password"
              className="mb-1.5 block text-sm font-medium text-slate-700">
              Confirm Password
            </label>
            <input
              id="confirm-password"
              type="password"
              autoComplete="new-password"
              minLength={6}
              value={confirmPassword}
              onChange={(event) => setConfirmPassword(event.target.value)}
              required
              className="w-full rounded-xl border border-slate-300 bg-slate-50 px-4 py-2.5 text-slate-800 outline-none transition-all duration-200 focus:border-blue-500 focus:bg-white focus:ring-4 focus:ring-blue-100"
            />
          </div>

          <button
            type="submit"
            disabled={isLoading}
            className="w-full rounded-xl bg-gradient-to-r from-blue-600 to-indigo-600 px-4 py-3 text-base font-semibold text-white shadow-lg shadow-blue-500/20 transition-all duration-200 hover:-translate-y-0.5 disabled:cursor-not-allowed disabled:opacity-75">
            {isLoading ? "Saving password..." : "Save Password"}
          </button>
        </form>
      </div>
    </div>
  );
};

export default FirstTimeAdminPasswordPage;
