import { useState, useEffect } from "react";
import { useAuth } from "../context/AuthContext";
import { useNavigate } from "react-router-dom";

const NAV = [
  { key: "dashboard", label: "Dashboard",
    icon: (
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
        <rect x="3" y="3" width="7" height="7" rx="1.5" stroke="currentColor" strokeWidth="2"/>
        <rect x="14" y="3" width="7" height="7" rx="1.5" stroke="currentColor" strokeWidth="2"/>
        <rect x="3" y="14" width="7" height="7" rx="1.5" stroke="currentColor" strokeWidth="2"/>
        <rect x="14" y="14" width="7" height="7" rx="1.5" stroke="currentColor" strokeWidth="2"/>
      </svg>
    ),
  },
  { key: "groups", label: "Groups",
    icon: (
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
        <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
        <circle cx="9" cy="7" r="4" stroke="currentColor" strokeWidth="2"/>
        <path d="M23 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
      </svg>
    ),
  },
  { key: "activity", label: "Activity",
    icon: (
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
        <polyline points="22 12 18 12 15 21 9 3 6 12 2 12" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
      </svg>
    ),
  },
  { key: "profile", label: "Profile",
    icon: (
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
        <circle cx="12" cy="8" r="4" stroke="currentColor" strokeWidth="2"/>
        <path d="M4 20c0-4 3.6-7 8-7s8 3 8 7" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
      </svg>
    ),
  },
  { key: "settings", label: "Settings",
    icon: (
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
        <circle cx="12" cy="12" r="3" stroke="currentColor" strokeWidth="2"/>
        <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83-2.83l.06-.06A1.65 1.65 0 0 0 4.68 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 2.83-2.83l.06.06A1.65 1.65 0 0 0 9 4.68a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 2.83l-.06.06A1.65 1.65 0 0 0 19.4 9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z" stroke="currentColor" strokeWidth="2"/>
      </svg>
    ),
  },
];

const GROUPS = [
  { name: "Roommates",     members: 2, total: "2,450 total", balance: "+992.21",  positive: true  },
  { name: "Office Lunches", members: 5, total: "5,500 total", balance: "+732.21", positive: true  },
];

const ACTIVITY = [
  { desc: "You added Pizza",            sub: "Office Lunches  2026-02-27", amount: "500.00",  share: "+400.00",  pos: true  },
  { desc: "You added Internet Bill",    sub: "Roommates  2026-02-21",      amount: "1000.00", share: "+500.00",  pos: true  },
  { desc: "Jake settled up Internet Bill", sub: "Roommates  2026-02-21",   amount: "1000.00", share: "+500.00",  pos: true  },
  { desc: "Maria added Jollibee",       sub: "Office Lunches  2026-02-14", amount: "1200.00", share: "240.00", pos: false },
];

export default function Dashboard() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [active, setActive] = useState("dashboard");
  const [showLogoutModal, setShowLogoutModal] = useState(false);

  // Redirect admins to admin console
  useEffect(() => {
    if (user?.role === "ROLE_ADMIN") {
      navigate("/admin", { replace: true });
    }
  }, [user, navigate]);

  const confirmLogout = () => { logout(); navigate("/login", { replace: true }); };

  return (
    <div className="flex h-screen overflow-hidden" style={{ background: "#f5f4f8" }}>

      {/*  Sidebar  */}
      <aside className="flex flex-col w-56 shrink-0 py-6 px-4" style={{ background: "#1e1030" }}>
        {/* Logo */}
        <div className="flex items-center gap-3 px-2 mb-10">
          <div className="w-9 h-9 rounded-xl flex items-center justify-center"
               style={{ background: "rgba(201,190,255,0.15)" }}>
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
              <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"
                    stroke="#C9BEFF" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
            </svg>
          </div>
          <span className="font-bold text-base" style={{ color: "#FFDBFD" }}>SplitShare</span>
        </div>

        {/* Nav */}
        <nav className="flex flex-col gap-1 flex-1">
          {NAV.map(item => {
            const isActive = active === item.key;
            return (
              <button
                key={item.key}
                onClick={() => setActive(item.key)}
                className="flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium transition cursor-pointer w-full text-left"
                style={{
                  background: isActive ? "#662498" : "transparent",
                  color: isActive ? "#FFDBFD" : "#9d8cbd",
                }}
                onMouseEnter={e => { if (!isActive) (e.currentTarget as HTMLElement).style.background = "rgba(255,255,255,0.06)"; }}
                onMouseLeave={e => { if (!isActive) (e.currentTarget as HTMLElement).style.background = "transparent"; }}
              >
                {item.icon}
                {item.label}
              </button>
            );
          })}
        </nav>

        {/* Sign out */}
        <button
          onClick={() => setShowLogoutModal(true)}
          className="flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium transition cursor-pointer"
          style={{ color: "#9d8cbd" }}
          onMouseEnter={e => { (e.currentTarget as HTMLElement).style.background = "rgba(255,255,255,0.06)"; (e.currentTarget as HTMLElement).style.color = "#FFDBFD"; }}
          onMouseLeave={e => { (e.currentTarget as HTMLElement).style.background = "transparent"; (e.currentTarget as HTMLElement).style.color = "#9d8cbd"; }}
        >
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
            <polyline points="16 17 21 12 16 7" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
            <line x1="21" y1="12" x2="9" y2="12" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
          </svg>
          Sign out
        </button>
      </aside>

      {/* Logout confirmation modal */}
      {showLogoutModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center"
             style={{ background: "rgba(0,0,0,0.5)" }}
             onClick={() => setShowLogoutModal(false)}>
          <div className="bg-white rounded-2xl shadow-2xl p-8 w-full max-w-sm mx-4"
               onClick={e => e.stopPropagation()}>
            <div className="flex items-center justify-center w-14 h-14 rounded-full mx-auto mb-5"
                 style={{ background: "#f5f0ff" }}>
              <svg width="28" height="28" viewBox="0 0 24 24" fill="none">
                <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" stroke="#662498" strokeWidth="2" strokeLinecap="round"/>
                <polyline points="16 17 21 12 16 7" stroke="#662498" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                <line x1="21" y1="12" x2="9" y2="12" stroke="#662498" strokeWidth="2" strokeLinecap="round"/>
              </svg>
            </div>
            <h3 className="text-lg font-bold text-gray-900 text-center">Sign out?</h3>
            <p className="text-sm text-gray-400 text-center mt-1 mb-7">
              Are you sure you want to sign out of SplitShare?
            </p>
            <div className="flex gap-3">
              <button
                onClick={() => setShowLogoutModal(false)}
                className="flex-1 py-2.5 rounded-xl text-sm font-semibold border border-gray-200 text-gray-600 hover:bg-gray-50 transition cursor-pointer"
              >
                Cancel
              </button>
              <button
                onClick={confirmLogout}
                className="flex-1 py-2.5 rounded-xl text-sm font-bold text-white transition cursor-pointer"
                style={{ background: "#662498" }}
                onMouseEnter={e => ((e.currentTarget as HTMLElement).style.background = "#4a1870")}
                onMouseLeave={e => ((e.currentTarget as HTMLElement).style.background = "#662498")}
              >
                Sign out
              </button>
            </div>
          </div>
        </div>
      )}

      {/*  Main  */}
      <main className="flex-1 overflow-y-auto">
        {/* Breadcrumb */}
        <div className="px-8 pt-6 pb-0">
          <p className="text-xs font-bold tracking-widest uppercase" style={{ color: "#9d8cbd" }}>
            Dashboard
          </p>
        </div>

        <div className="px-8 py-6 space-y-8">

          {/* Header row */}
          <div className="flex items-start justify-between">
            <div>
              <h1 className="text-3xl font-bold text-gray-900">
                Hello, {user?.firstname}!
              </h1>
              <p className="text-sm text-gray-400 mt-1">Here's your expense summary</p>
            </div>
            <div className="flex items-center gap-3">
              {user?.role === "ROLE_ADMIN" && (
                <button
                  onClick={() => navigate("/admin")}
                  className="flex items-center gap-2 px-5 py-2.5 text-sm font-bold rounded-xl transition cursor-pointer"
                  style={{ background: "#e9ddff", color: "#4a1870" }}
                  onMouseEnter={e => ((e.currentTarget as HTMLElement).style.background = "#dccbff")}
                  onMouseLeave={e => ((e.currentTarget as HTMLElement).style.background = "#e9ddff")}
                >
                  Admin Console
                </button>
              )}
              <button
                className="flex items-center gap-2 px-5 py-2.5 text-sm font-bold text-white rounded-xl transition cursor-pointer"
                style={{ background: "#662498" }}
                onMouseEnter={e => ((e.currentTarget as HTMLElement).style.background = "#4a1870")}
                onMouseLeave={e => ((e.currentTarget as HTMLElement).style.background = "#662498")}
              >
                + Add Expense
              </button>
            </div>
          </div>

          {/* Stat cards */}
          <div className="grid grid-cols-3 gap-5">
            {[
              { label: "Net Balance",   value: "+1,484.42", sub: "You are owed overall", color: "#662498", bg: "#f5f0ff" },
              { label: "You are owed",  value: "+1,724.42", sub: "You are owed overall", color: "#16a34a", bg: "#f0fdf4" },
              { label: "You owe",       value: "240.00",    sub: "You are owed overall", color: "#dc2626", bg: "#fef2f2" },
            ].map(c => (
              <div key={c.label} className="rounded-2xl p-5 border border-gray-100 shadow-sm"
                   style={{ background: c.bg }}>
                <p className="text-xs font-semibold text-gray-400 uppercase tracking-wide mb-3">{c.label}</p>
                <p className="text-2xl font-bold" style={{ color: c.color }}>{c.value}</p>
                <p className="text-xs text-gray-400 mt-1">{c.sub}</p>
              </div>
            ))}
          </div>

          {/* Groups */}
          <div>
            <h2 className="text-base font-bold text-gray-800 mb-4">Your Groups</h2>
            <div className="grid grid-cols-2 gap-5">
              {GROUPS.map((g, i) => (
                <div key={g.name} className="rounded-2xl p-5 cursor-pointer"
                     style={{ background: i === 0 ? "#C9BEFF" : "#BCD3F2" }}>
                  <p className="font-bold text-gray-900">{g.name}</p>
                  <p className="text-xs text-gray-600 mt-0.5">{g.members} members</p>
                  <div className="flex items-end justify-between mt-6">
                    <p className="text-xs text-gray-600">{g.total}</p>
                    <p className="text-sm font-bold" style={{ color: "#16a34a" }}>{g.balance}<br/>
                      <span className="text-xs font-normal text-gray-600">you are owed</span>
                    </p>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Recent Activity */}
          <div>
            <h2 className="text-base font-bold text-gray-800 mb-4">Recent Activity</h2>
            <div className="bg-white rounded-2xl border border-gray-100 shadow-sm divide-y divide-gray-50">
              {ACTIVITY.map((a, i) => (
                <div key={i} className="flex items-center justify-between px-6 py-4">
                  <div>
                    <p className="text-sm font-medium text-gray-800">{a.desc}</p>
                    <p className="text-xs text-gray-400 mt-0.5">{a.sub}</p>
                  </div>
                  <div className="text-right">
                    <p className="text-sm font-semibold text-gray-700">{a.amount}</p>
                    <p className="text-xs font-bold mt-0.5" style={{ color: a.pos ? "#16a34a" : "#dc2626" }}>
                      {a.share}
                    </p>
                  </div>
                </div>
              ))}
            </div>
          </div>

        </div>
      </main>
    </div>
  );
}
