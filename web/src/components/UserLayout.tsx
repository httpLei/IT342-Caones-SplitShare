import { useMemo, useState } from "react";
import { Link, Outlet, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

const NAV = [
  { key: "dashboard", label: "Dashboard", path: "/dashboard" },
  { key: "connections", label: "Connections", path: "/connections" },
  { key: "groups", label: "Groups", path: "/groups" },
  { key: "activity", label: "Activity", path: "/activity" },
  { key: "profile", label: "Profile", path: "/profile" },
  { key: "settings", label: "Settings", path: "/settings" }
];

export default function UserLayout() {
  const location = useLocation();
  const navigate = useNavigate();
  const { logout } = useAuth();
  const [showLogoutModal, setShowLogoutModal] = useState(false);

  const activePath = useMemo(() => {
    const current = NAV.find((item) => location.pathname.startsWith(item.path));
    return current?.path ?? "/dashboard";
  }, [location.pathname]);

  const breadcrumb = useMemo(() => {
    const current = NAV.find((item) => item.path === activePath);
    return current?.label ?? "Dashboard";
  }, [activePath]);

  const confirmLogout = () => {
    logout();
    navigate("/login", { replace: true });
  };

  return (
    <div className="flex h-screen overflow-hidden" style={{ background: "#f5f4f8" }}>
      <aside className="flex flex-col w-56 shrink-0 py-6 px-4" style={{ background: "#1e1030" }}>
        <div className="flex items-center gap-3 px-2 mb-10">
          <div className="w-9 h-9 rounded-xl flex items-center justify-center" style={{ background: "rgba(201,190,255,0.15)" }}>
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
              <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5" stroke="#C9BEFF" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
            </svg>
          </div>
          <span className="font-bold text-base" style={{ color: "#FFDBFD" }}>SplitShare</span>
        </div>

        <nav className="flex flex-col gap-1 flex-1">
          {NAV.map((item) => {
            const isActive = activePath === item.path;
            return (
              <Link
                key={item.key}
                to={item.path}
                className="flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium transition"
                style={{
                  background: isActive ? "#662498" : "transparent",
                  color: isActive ? "#FFDBFD" : "#9d8cbd"
                }}
              >
                {item.label}
              </Link>
            );
          })}
        </nav>

        <button
          onClick={() => setShowLogoutModal(true)}
          className="flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium transition cursor-pointer"
          style={{ color: "#9d8cbd" }}
          onMouseEnter={(e) => {
            (e.currentTarget as HTMLElement).style.background = "rgba(255,255,255,0.06)";
            (e.currentTarget as HTMLElement).style.color = "#FFDBFD";
          }}
          onMouseLeave={(e) => {
            (e.currentTarget as HTMLElement).style.background = "transparent";
            (e.currentTarget as HTMLElement).style.color = "#9d8cbd";
          }}
        >
          Sign out
        </button>
      </aside>

      {showLogoutModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center" style={{ background: "rgba(0,0,0,0.5)" }} onClick={() => setShowLogoutModal(false)}>
          <div className="bg-white rounded-2xl shadow-2xl p-8 w-full max-w-sm mx-4" onClick={(e) => e.stopPropagation()}>
            <h3 className="text-lg font-bold text-gray-900 text-center">Sign out?</h3>
            <p className="text-sm text-gray-400 text-center mt-1 mb-7">Are you sure you want to sign out of SplitShare?</p>
            <div className="flex gap-3">
              <button onClick={() => setShowLogoutModal(false)} className="flex-1 py-2.5 rounded-xl text-sm font-semibold border border-gray-200 text-gray-600 hover:bg-gray-50 transition cursor-pointer">
                Cancel
              </button>
              <button
                onClick={confirmLogout}
                className="flex-1 py-2.5 rounded-xl text-sm font-bold text-white transition cursor-pointer"
                style={{ background: "#662498" }}
                onMouseEnter={(e) => ((e.currentTarget as HTMLElement).style.background = "#4a1870")}
                onMouseLeave={(e) => ((e.currentTarget as HTMLElement).style.background = "#662498")}
              >
                Sign out
              </button>
            </div>
          </div>
        </div>
      )}

      <main className="flex-1 overflow-y-auto">
        <div className="px-8 pt-6 pb-0">
          <p className="text-xs font-bold tracking-widest uppercase" style={{ color: "#9d8cbd" }}>{breadcrumb}</p>
        </div>
        <div className="px-8 py-6">
          <Outlet />
        </div>
      </main>
    </div>
  );
}
