import { useMemo, useState } from "react";
import { Link, Outlet, useLocation, useNavigate } from "react-router-dom";
import { LayoutDashboard, Users, Activity, User, Settings, LogOut, Menu, X } from "lucide-react";
import { useAuth } from "../../auth/AuthContext";
import { useTheme } from "../../settings/contexts/ThemeContext";

const NAV = [
  { key: "dashboard", label: "Dashboard", path: "/dashboard", icon: LayoutDashboard },
  { key: "groups", label: "Groups", path: "/groups", icon: Users },
  { key: "activity", label: "Activity", path: "/activity", icon: Activity },
  { key: "profile", label: "Profile", path: "/profile", icon: User },
  { key: "settings", label: "Settings", path: "/settings", icon: Settings }
];

export default function UserLayout() {
  const location = useLocation();
  const navigate = useNavigate();
  const { logout } = useAuth();
  const { isDark } = useTheme();
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

  const [sidebarOpen, setSidebarOpen] = useState(false);

  return (
    <div className="flex h-screen overflow-hidden dark:bg-gray-950 transition-colors duration-300" style={{ background: isDark ? "#0f172a" : "#f5f4f8" }}>
      {/* Mobile menu button */}
      <div className="md:hidden fixed top-4 left-4 z-40">
        <button
          onClick={() => setSidebarOpen(!sidebarOpen)}
          className="p-2 rounded-lg transition"
          style={{ background: "#1e1030", color: "#FFDBFD" }}
        >
          {sidebarOpen ? <X size={24} /> : <Menu size={24} />}
        </button>
      </div>

      {/* Sidebar overlay for mobile */}
      {sidebarOpen && (
        <div
          className="fixed inset-0 md:hidden z-30"
          style={{ background: "rgba(0,0,0,0.5)" }}
          onClick={() => setSidebarOpen(false)}
        />
      )}

      <aside className={`${sidebarOpen ? 'translate-x-0' : '-translate-x-full'} md:translate-x-0 flex flex-col w-56 shrink-0 py-6 px-4 fixed md:relative h-screen z-30 transition-transform duration-300`} style={{ background: isDark ? "#1a1626" : "#f9f5ff" }}>
        <div className="flex items-center gap-3 px-2 mb-10">
          <div className="w-10 h-10 rounded-xl flex items-center justify-center" style={{ background: isDark ? "rgba(201,190,255,0.2)" : "rgba(102, 36, 152, 0.1)" }}>
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
              <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5" stroke={isDark ? "#C9BEFF" : "#662498"} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
            </svg>
          </div>
          <span className="font-bold text-base" style={{ color: isDark ? "#FFDBFD" : "#662498" }}>SplitShare</span>
        </div>

        <nav className="flex flex-col gap-2 flex-1">
          {NAV.map((item) => {
            const isActive = activePath === item.path;
            const Icon = item.icon;
            return (
              <Link
                key={item.key}
                to={item.path}
                onClick={() => setSidebarOpen(false)}
                className="flex items-center gap-3 px-3 py-3 rounded-xl text-sm font-medium transition duration-200"
                style={{
                  background: isActive ? (isDark ? "rgba(102, 36, 152, 0.3)" : "rgba(102, 36, 152, 0.1)") : "transparent",
                  color: isActive ? (isDark ? "#FFDBFD" : "#662498") : (isDark ? "#9d8cbd" : "#8b7b9e"),
                  borderLeft: isActive ? `3px solid ${isDark ? "#c9a0ff" : "#662498"}` : "3px solid transparent"
                }}
              >
                <Icon size={20} className="flex-shrink-0" />
                <span>{item.label}</span>
              </Link>
            );
          })}
        </nav>

        <button
          onClick={() => setShowLogoutModal(true)}
          className="flex items-center gap-3 px-3 py-3 rounded-xl text-sm font-medium transition duration-200 cursor-pointer"
          style={{ color: isDark ? "#9d8cbd" : "#8b7b9e" }}
          onMouseEnter={(e) => {
            (e.currentTarget as HTMLElement).style.background = isDark ? "rgba(255,255,255,0.06)" : "rgba(102, 36, 152, 0.08)";
            (e.currentTarget as HTMLElement).style.color = isDark ? "#FFDBFD" : "#662498";
          }}
          onMouseLeave={(e) => {
            (e.currentTarget as HTMLElement).style.background = "transparent";
            (e.currentTarget as HTMLElement).style.color = isDark ? "#9d8cbd" : "#8b7b9e";
          }}
        >
          <LogOut size={20} className="flex-shrink-0" />
          <span>Sign out</span>
        </button>
      </aside>

      {showLogoutModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center" style={{ background: "rgba(0,0,0,0.5)" }} onClick={() => setShowLogoutModal(false)}>
          <div className="bg-white dark:bg-gray-900 rounded-2xl shadow-2xl p-8 w-full max-w-sm mx-4 transition-colors duration-300" onClick={(e) => e.stopPropagation()}>
            <h3 className="text-lg font-bold text-gray-900 dark:text-white text-center">Sign out?</h3>
            <p className="text-sm text-gray-500 dark:text-gray-400 text-center mt-1 mb-7">Are you sure you want to sign out of SplitShare?</p>
            <div className="flex gap-3">
              <button onClick={() => setShowLogoutModal(false)} className="flex-1 py-2.5 rounded-xl text-sm font-semibold border border-gray-200 dark:border-gray-700 text-gray-600 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-800 transition cursor-pointer">
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

      <main className="flex-1 overflow-y-auto dark:bg-gray-900 transition-colors duration-300" style={{ background: isDark ? "#111827" : "white" }}>
        <div className="px-8 pt-6 pb-0">
          <p className="text-xs font-bold tracking-widest uppercase dark:text-gray-500" style={{ color: isDark ? "#9ca3af" : "#9d8cbd" }}>{breadcrumb}</p>
        </div>
        <div className="px-8 py-6">
          <Outlet />
        </div>
      </main>
    </div>
  );
}
