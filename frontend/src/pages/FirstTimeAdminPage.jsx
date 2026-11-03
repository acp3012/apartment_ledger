import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import api from "../api/axiosConfig";

const FirstTimeAdminPage = () => {
  const navigate = useNavigate();
  const [emailId, setEmailId] = useState("");
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError("");
    setIsLoading(true);

    try {
      const response = await api.post("/auth/admin-login", { emailId });
      navigate("/first-time-admin/password", {
        state: { emailId: response.data?.email || emailId },
      });
    } catch (requestError) {
      setError(
        requestError.response?.data?.message ||
          requestError.message ||
          "Unable to verify this administrator account.",
      );
    } finally {
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
            Administrator setup
          </h1>
          <p className="mt-2 text-sm text-slate-500">
            Verify your administrator email to set your password.
          </p>
        </div>

        {error && (
          <div
            role="alert"
            className="mb-6 rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700">
            {error}
          </div>
        )}

        <form
          onSubmit={handleSubmit}
          className="space-y-5">
          <div>
            <label
              htmlFor="admin-email"
              className="mb-1.5 block text-sm font-medium text-slate-700">
              Email Address
            </label>
            <input
              id="admin-email"
              type="email"
              autoComplete="email"
              value={emailId}
              onChange={(event) => setEmailId(event.target.value)}
              required
              className="w-full rounded-xl border border-slate-300 bg-slate-50 px-4 py-2.5 text-slate-800 outline-none transition-all duration-200 focus:border-blue-500 focus:bg-white focus:ring-4 focus:ring-blue-100"
            />
          </div>

          <button
            type="submit"
            disabled={isLoading}
            className="w-full rounded-xl bg-gradient-to-r from-blue-600 to-indigo-600 px-4 py-3 text-base font-semibold text-white shadow-lg shadow-blue-500/20 transition-all duration-200 hover:-translate-y-0.5 disabled:cursor-not-allowed disabled:opacity-75">
            {isLoading ? "Verifying..." : "Continue"}
          </button>
        </form>

        <div className="mt-6 text-center text-sm text-slate-600">
          Already set your password?{" "}
          <Link
            to="/login"
            className="font-medium text-blue-600 hover:text-blue-700 hover:underline">
            Log in
          </Link>
        </div>
      </div>
    </div>
  );
};

export default FirstTimeAdminPage;
