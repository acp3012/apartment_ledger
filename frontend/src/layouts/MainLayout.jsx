import { useState } from "react";
import { Navigate, Outlet, useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

const MainLayout = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { user, logout } = useAuth();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const isAdmin = Array.isArray(user?.roleNames)
    ? user.roleNames.some((role) => role === "ADMIN" || role === "ROLE_ADMIN")
    : user?.isAdmin === true;
  const isExpenseRoute =
    location.pathname === "/dashboard/expense" ||
    location.pathname.startsWith("/dashboard/expense/");
  const isAdminRoute = location.pathname.startsWith("/admin/");
  const isOneTimeSetupRoute =
    (location.pathname.startsWith("/admin/setup") &&
      location.pathname !== "/admin/setup/flats") ||
    location.pathname === "/admin/opening-balance";

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  const isActive = (path) =>
    path === "/dashboard"
      ? location.pathname === path
      : location.pathname === path || location.pathname.startsWith(`${path}/`);

  // Sidebar item styling with active indicator
  const sidebarItemClass = (path) =>
    `flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-medium transition-colors ${
      isActive(path)
        ? "bg-blue-50 text-blue-600 font-semibold border-l-4 border-blue-600 rounded-l-none"
        : "text-slate-600 hover:bg-slate-100 hover:text-slate-900"
    }`;

  if (!user) {
    return (
      <Navigate
        to="/login"
        replace
      />
    );
  }
  if ((isExpenseRoute || isAdminRoute) && !isAdmin) {
    return (
      <Navigate
        to="/dashboard"
        replace
      />
    );
  }
  if (isOneTimeSetupRoute && user.isSetupCompleted) {
    return (
      <Navigate
        to="/dashboard"
        replace
      />
    );
  }

  return (
    <div className="min-h-screen bg-slate-100 font-sans text-slate-800 flex">
      {/* FIXED LEFT SIDEBAR */}
      <aside className="hidden lg:flex flex-col w-72 bg-white border-r border-slate-200 sticky top-0 h-screen z-30 shadow-sm">
        {/* Brand Logo & Apartment Header */}
        <div
          className="p-6 border-b border-slate-100 flex flex-col items-center text-center cursor-pointer"
          onClick={() => navigate("/dashboard")}>
          <div className="w-14 h-14 rounded-2xl bg-gradient-to-br from-blue-600 to-indigo-700 flex items-center justify-center text-white text-xl font-bold shadow-md mb-3">
            L
          </div>
          <h1 className="text-base font-bold text-slate-900 tracking-tight">
            LedgerApp
          </h1>
          <p className="text-xs text-slate-500 mt-0.5">{user.apartmentName}</p>
        </div>

        {/* Navigation Links */}
        <div className="flex-1 py-6 px-4 space-y-1.5 overflow-y-auto">
          <p className="px-4 text-[11px] font-bold uppercase tracking-wider text-slate-400 mb-2">
            Modules
          </p>

          <button
            onClick={() => navigate("/dashboard")}
            className={`w-full text-left ${sidebarItemClass("/dashboard")}`}>
            <span>🏠</span> Dashboard
          </button>

          <button
            onClick={() => navigate("/monthly-statement")}
            className={`w-full text-left ${sidebarItemClass("/monthly-statement")}`}>
            <span>📊</span> Monthly Report
          </button>

          {isAdmin && (
            <>
              <button
                onClick={() => navigate("/dashboard/expense")}
                className={`w-full text-left ${sidebarItemClass("/dashboard/expense")}`}>
                <span>💸</span> Ledger Entries
              </button>
              {!user.isSetupCompleted && (
                <>
                  <p className="px-4 pt-5 text-[11px] font-bold uppercase tracking-wider text-slate-400">
                    One-Time Setup
                  </p>
                  <button
                    onClick={() => navigate("/admin/setup")}
                    className={`w-full text-left ${sidebarItemClass("/admin/setup")}`}>
                    <span>🧰</span> One-Time Setup
                  </button>
                  <button
                    onClick={() => navigate("/admin/opening-balance")}
                    className={`w-full text-left ${sidebarItemClass("/admin/opening-balance")}`}>
                    <span>＋</span> Opening Balance
                  </button>
                </>
              )}
              {user.isSetupCompleted && (
                <button
                  onClick={() => navigate("/admin/setup/flats")}
                  className={`w-full text-left ${sidebarItemClass("/admin/setup/flats")}`}>
                  <span>🏢</span> Flats
                </button>
              )}
              <p className="px-4 pt-5 text-[11px] font-bold uppercase tracking-wider text-slate-400">
                Maintenance
              </p>
              <button
                onClick={() => navigate("/admin/maintenance-fees")}
                className={`w-full text-left ${sidebarItemClass("/admin/maintenance-fees")}`}>
                <span>💰</span> Maintenance Fees
              </button>
              <p className="px-4 pt-5 text-[11px] font-bold uppercase tracking-wider text-slate-400">
                Administration
              </p>
              <button
                onClick={() => navigate("/admin/master-entries")}
                className={`w-full text-left ${sidebarItemClass("/admin/master-entries")}`}>
                <span>⚙️</span> Master Entries
              </button>
            </>
          )}
        </div>

        {/* Footer info */}
        <div className="p-4 border-t border-slate-100 text-center">
          <p className="text-[11px] text-slate-400">Apartment Ledger v2.0</p>
        </div>
      </aside>

      {/* MAIN VIEWPORT CONTAINER */}
      <div className="flex-1 flex flex-col min-w-0">
        {/* TOP FLOATING PROFILE & HEADER */}
        <header className="bg-white border-b border-slate-200 sticky top-0 z-20 px-6 py-3.5 shadow-xs flex items-center justify-between">
          {/* Mobile hamburger toggle */}
          <div className="flex items-center gap-3 lg:hidden">
            <button
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
              className="p-2 text-slate-600 rounded-lg hover:bg-slate-100 font-bold">
              ☰
            </button>
            <span className="font-bold text-slate-900">LedgerApp</span>
          </div>

          <div className="hidden lg:block">
            <span className="text-xs font-semibold uppercase tracking-wider text-slate-400">
              Enterprise Accounting Portal
            </span>
          </div>

          {/* User Profile Pill & Logout */}
          <div className="flex items-center gap-2 sm:gap-4 ml-auto">
            <div className="flex items-center gap-2 sm:gap-3 bg-slate-50 border border-slate-200 px-2 sm:px-3.5 py-1.5 rounded-2xl shadow-xs">
              <div className="w-8 h-8 rounded-full bg-blue-600 text-white flex items-center justify-center font-bold text-xs shrink-0">
                {user.displayName?.charAt(0) || "U"}
              </div>
              <div className="text-left leading-tight hidden sm:block">
                <p className="text-xs font-bold text-slate-900">
                  {user.displayName}
                </p>
                <p className="text-[10px] text-slate-500 font-medium">
                  {isAdmin ? "Administrator" : `Flat ${user.flatNumber}`}
                </p>
              </div>
            </div>

            <button
              onClick={handleLogout}
              className="bg-white border border-slate-200 hover:bg-red-50 hover:border-red-200 hover:text-red-600 text-slate-600 px-2.5 sm:px-3.5 py-2 rounded-xl text-xs font-semibold transition-colors shadow-xs shrink-0">
              Log Out
            </button>
          </div>
        </header>

        {/* Mobile Navigation Drawer */}
        {mobileMenuOpen && (
          <div className="lg:hidden bg-white border-b border-slate-200 p-4 space-y-2 shadow-md z-40">
            <button
              onClick={() => {
                navigate("/dashboard");
                setMobileMenuOpen(false);
              }}
              className="block w-full text-left px-3 py-2 rounded-lg text-sm font-medium text-slate-700 hover:bg-slate-50">
              Dashboard
            </button>
            <button
              onClick={() => {
                navigate("/monthly-statement");
                setMobileMenuOpen(false);
              }}
              className="block w-full text-left px-3 py-2 rounded-lg text-sm font-medium text-slate-700 hover:bg-slate-50">
              Monthly Report
            </button>
            {isAdmin && (
              <>
                <button
                  onClick={() => {
                    navigate("/dashboard/expense");
                    setMobileMenuOpen(false);
                  }}
                  className="block w-full text-left px-3 py-2 rounded-lg text-sm font-medium text-slate-700 hover:bg-slate-50">
                  Ledger Entries
                </button>
                {!user.isSetupCompleted && (
                  <>
                    <p className="px-3 pt-3 text-[11px] font-bold uppercase tracking-wider text-slate-400">
                      One-Time Setup
                    </p>
                    <button
                      onClick={() => {
                        navigate("/admin/setup");
                        setMobileMenuOpen(false);
                      }}
                      className="block w-full text-left px-3 py-2 rounded-lg text-sm font-medium text-slate-700 hover:bg-slate-50">
                      Setup Overview
                    </button>
                    <button
                      onClick={() => {
                        navigate("/admin/setup/flats");
                        setMobileMenuOpen(false);
                      }}
                      className="block w-full text-left px-3 py-2 rounded-lg text-sm font-medium text-slate-700 hover:bg-slate-50">
                      Create Flats
                    </button>
                    <button
                      onClick={() => {
                        navigate("/admin/setup/period");
                        setMobileMenuOpen(false);
                      }}
                      className="block w-full text-left px-3 py-2 rounded-lg text-sm font-medium text-slate-700 hover:bg-slate-50">
                      Starting Period
                    </button>
                    <button
                      onClick={() => {
                        navigate("/admin/opening-balance");
                        setMobileMenuOpen(false);
                      }}
                      className="block w-full text-left px-3 py-2 rounded-lg text-sm font-medium text-slate-700 hover:bg-slate-50">
                      Opening Balance
                    </button>
                  </>
                )}
                {user.isSetupCompleted && (
                  <button
                    onClick={() => {
                      navigate("/admin/setup/flats");
                      setMobileMenuOpen(false);
                    }}
                    className="block w-full text-left px-3 py-2 rounded-lg text-sm font-medium text-slate-700 hover:bg-slate-50">
                    Flats
                  </button>
                )}
                <p className="px-3 pt-3 text-[11px] font-bold uppercase tracking-wider text-slate-400">
                  Maintenance
                </p>
                <button
                  onClick={() => {
                    navigate("/admin/maintenance-fees");
                    setMobileMenuOpen(false);
                  }}
                  className="block w-full text-left px-3 py-2 rounded-lg text-sm font-medium text-slate-700 hover:bg-slate-50">
                  Maintenance Fees
                </button>
                <p className="px-3 pt-3 text-[11px] font-bold uppercase tracking-wider text-slate-400">
                  Administration
                </p>
                <button
                  onClick={() => {
                    navigate("/admin/master-entries");
                    setMobileMenuOpen(false);
                  }}
                  className="block w-full text-left px-3 py-2 rounded-lg text-sm font-medium text-slate-700 hover:bg-slate-50">
                  Master Entries
                </button>
              </>
            )}
          </div>
        )}

        {/* UNIFORM SCROLLABLE CONTENT AREA */}
        <main className="flex-1 p-4 sm:p-6 md:p-8 max-w-[1400px] w-full mx-auto">
          <Outlet />
        </main>
      </div>
    </div>
  );
};

export default MainLayout;
