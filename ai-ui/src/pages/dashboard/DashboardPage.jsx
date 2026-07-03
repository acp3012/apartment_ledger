import { useNavigate } from "react-router-dom";
import { useAuth }     from "../../context/AuthContext";
import { BuildingIcon } from "../../components/UI";

// Placeholder cards for future pages
const MODULES = [
  { icon: "💰", title: "Monthly Income",  desc: "Coming soon" },
  { icon: "📋", title: "Expenses",        desc: "Coming soon" },
  { icon: "👥", title: "Flat Owners",     desc: "Coming soon" },
  { icon: "📊", title: "Reports",         desc: "Coming soon" },
];

function formatLoginTime(iso) {
  if (!iso) return "—";
  const d = new Date(iso);
  return d.toLocaleString("en-IN", {
    day:    "2-digit",
    month:  "short",
    year:   "numeric",
    hour:   "2-digit",
    minute: "2-digit",
    hour12: true,
  });
}

function initials(name) {
  if (!name) return "?";
  return name.split(" ").map(w => w[0]).join("").toUpperCase().slice(0, 2);
}

export default function DashboardPage() {
  const { session, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate("/");
  };

  const displayName    = session?.userName || session?.email || "User";
  const apartmentName  = session?.apartmentName || null;

  return (
    <div className="dashboard-wrap">

      {/* ── Top bar ───────────────────────────────────────── */}
      <header className="topbar">
        <div className="topbar-brand">
          <div className="topbar-brand-icon"><BuildingIcon size={18} /></div>
          ApartmentLedger
        </div>

        {apartmentName && (
          <span className="topbar-apt">🏢 {apartmentName}</span>
        )}

        <div className="topbar-right">
          <span className="topbar-user">{displayName}</span>
          <div className="topbar-avatar">{initials(displayName)}</div>
          <button className="btn-logout" onClick={handleLogout}>Sign out</button>
        </div>
      </header>

      {/* ── Main content ──────────────────────────────────── */}
      <main className="dashboard-main">

        {/* Welcome banner */}
        <div className="welcome-card">
          <h2>Welcome back, {displayName} 👋</h2>
          <p>
            {apartmentName
              ? `Managing ${apartmentName}`
              : "Select a module below to get started"}
          </p>
        </div>

        {/* Placeholder module cards */}
        <div className="empty-sections">
          {MODULES.map(m => (
            <div key={m.title} className="empty-card">
              <span className="empty-icon">{m.icon}</span>
              <span className="empty-title">{m.title}</span>
              <span>{m.desc}</span>
            </div>
          ))}
        </div>
      </main>

      {/* ── Status bar ────────────────────────────────────── */}
      <footer className="status-bar">
        <span>
          <span className="status-dot" />
          Connected
        </span>
        <span>
          Logged in as <strong>{session?.email}</strong>
        </span>
        <span style={{ marginLeft: "auto" }}>
          Login time: {formatLoginTime(session?.loginTime)}
        </span>
      </footer>

    </div>
  );
}
