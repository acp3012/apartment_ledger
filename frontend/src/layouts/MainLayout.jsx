import { useState } from "react";
import { Outlet, useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

const MainLayout = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { user, logout } = useAuth();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  const isActive = (path) => location.pathname.startsWith(path);

  // Sidebar item styling with active indicator
  const sidebarItemClass = (path) =>
    `flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-medium transition-colors ${
      isActive(path)
        ? "bg-blue-50 text-blue-600 font-semibold border-l-4 border-blue-600 rounded-l-none"
        : "text-slate-600 hover:bg-slate-100 hover:text-slate-900"
    }`;

  if (!user) return null;

  return (
    <div className="min-h-screen bg-slate-100 font-sans text-slate-800 flex">
      
      {/* FIXED LEFT SIDEBAR */}
      <aside className="hidden lg:flex flex-col w-72 bg-white border-r border-slate-200 sticky top-0 h-screen z-30 shadow-sm">
        {/* Brand Logo & Apartment Header */}
        <div className="p-6 border-b border-slate-100 flex flex-col items-center text-center cursor-pointer" onClick={() => navigate("/dashboard")}>
          <div className="w-14 h-14 rounded-2xl bg-gradient-to-br from-blue-600 to-indigo-700 flex items-center justify-center text-white text-xl font-bold shadow-md mb-3">
            L
          </div>
          <h1 className="text-base font-bold text-slate-900 tracking-tight">LedgerApp</h1>
          <p className="text-xs text-slate-500 mt-0.5">{user.apartmentName}</p>
        </div>

        {/* Navigation Links */}
        <div className="flex-1 py-6 px-4 space-y-1.5 overflow-y-auto">
          <p className="px-4 text-[11px] font-bold uppercase tracking-wider text-slate-400 mb-2">Modules</p>
          
          <button onClick={() => navigate("/dashboard")} className={`w-full text-left ${sidebarItemClass("/dashboard")}`}>
            <span>🏠</span> Dashboard
          </button>

          <button onClick={() => navigate("/monthly-statement")} className={`w-full text-left ${sidebarItemClass("/monthly-statement")}`}>
            <span>📊</span> Monthly Report
          </button>

          {user.isAdmin && (
            <>
              <button onClick={() => navigate(`/apartments/${user.apartmentId}/incomes`)} className={`w-full text-left ${sidebarItemClass("/apartments")}`}>
                <span>💰</span> Income Management
              </button>

              <button onClick={() => navigate("/dashboard/expense")} className={`w-full text-left ${sidebarItemClass("/dashboard/expense")}`}>
                <span>💸</span> Expense Management
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
            <button onClick={() => setMobileMenuOpen(!mobileMenuOpen)} className="p-2 text-slate-600 rounded-lg hover:bg-slate-100 font-bold">
              ☰
            </button>
            <span className="font-bold text-slate-900">LedgerApp</span>
          </div>

          <div className="hidden lg:block">
            <span className="text-xs font-semibold uppercase tracking-wider text-slate-400">Enterprise Accounting Portal</span>
          </div>

          {/* User Profile Pill & Logout */}
          <div className="flex items-center gap-4 ml-auto">
            <div className="flex items-center gap-3 bg-slate-50 border border-slate-200 px-3.5 py-1.5 rounded-2xl shadow-xs">
              <div className="w-8 h-8 rounded-full bg-blue-600 text-white flex items-center justify-center font-bold text-xs">
                {user.displayName?.charAt(0) || "U"}
              </div>
              <div className="text-left leading-tight">
                <p className="text-xs font-bold text-slate-900">{user.displayName}</p>
                <p className="text-[10px] text-slate-500 font-medium">Flat {user.flatNumber} {user.isAdmin ? '• Admin' : ''}</p>
              </div>
            </div>

            <button
              onClick={handleLogout}
              className="bg-white border border-slate-200 hover:bg-red-50 hover:border-red-200 hover:text-red-600 text-slate-600 px-3.5 py-2 rounded-xl text-xs font-semibold transition-colors shadow-xs">
              Log Out
            </button>
          </div>
        </header>

        {/* Mobile Navigation Drawer */}
        {mobileMenuOpen && (
          <div className="lg:hidden bg-white border-b border-slate-200 p-4 space-y-2 shadow-md z-40">
            <button onClick={() => { navigate("/dashboard"); setMobileMenuOpen(false); }} className="block w-full text-left px-3 py-2 rounded-lg text-sm font-medium text-slate-700 hover:bg-slate-50">Dashboard</button>
            <button onClick={() => { navigate("/monthly-statement"); setMobileMenuOpen(false); }} className="block w-full text-left px-3 py-2 rounded-lg text-sm font-medium text-slate-700 hover:bg-slate-50">Monthly Report</button>
            {user.isAdmin && (
              <>
                <button onClick={() => { navigate(`/apartments/${user.apartmentId}/incomes`); setMobileMenuOpen(false); }} className="block w-full text-left px-3 py-2 rounded-lg text-sm font-medium text-slate-700 hover:bg-slate-50">Income Management</button>
                <button onClick={() => { navigate("/dashboard/expense/draft"); setMobileMenuOpen(false); }} className="block w-full text-left px-3 py-2 rounded-lg text-sm font-medium text-slate-700 hover:bg-slate-50">Expense Management</button>
              </>
            )}
          </div>
        )}

        {/* UNIFORM SCROLLABLE CONTENT AREA */}
        <main className="flex-1 p-6 md:p-8 max-w-[1400px] w-full mx-auto">
          <Outlet />
        </main>
      </div>
    </div>
  );
};

export default MainLayout;
