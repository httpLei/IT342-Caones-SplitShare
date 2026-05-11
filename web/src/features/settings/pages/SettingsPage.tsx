import { useState } from "react";
import { Sun, Moon, Monitor, Save, Bell, DollarSign } from "lucide-react";
import { useTheme } from "../ThemeContext";

export default function SettingsPage() {
  const { theme, setTheme, isDark } = useTheme();
  const [currency, setCurrency] = useState("PHP (Philippine Peso)");
  const [notificationsEnabled, setNotificationsEnabled] = useState(true);
  const [appSettingsNotice, setAppSettingsNotice] = useState("");

  const saveAppSettings = () => {
    setAppSettingsNotice("✓ Preferences saved successfully!");
    setTimeout(() => setAppSettingsNotice(""), 3000);
  };

  const themeOptions = [
    { value: "light", label: "Light", icon: Sun, description: "Bright and clean" },
    { value: "dark", label: "Dark", icon: Moon, description: "Easy on the eyes" },
    { value: "system", label: "System", icon: Monitor, description: "Follow device" }
  ];

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-4xl font-bold text-gray-900 dark:text-white">Settings ⚙️</h1>
        <p className="text-sm text-gray-500 dark:text-gray-400 mt-2">Customize your app experience</p>
      </div>

      <section className="bg-white dark:bg-gray-800 rounded-2xl border border-gray-100 dark:border-gray-700 shadow-sm p-6 transition duration-300">
        <h2 className="text-lg font-bold text-gray-900 dark:text-white mb-5 flex items-center gap-2">
          🎨 Appearance
        </h2>
        <p className="text-sm text-gray-600 dark:text-gray-400 mb-4">Choose your preferred theme</p>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
          {themeOptions.map((option) => {
            const isSelected = theme === option.value;
            const Icon = option.icon;
            return (
              <button
                key={option.value}
                onClick={() => setTheme(option.value as "light" | "dark" | "system")}
                className="px-6 py-4 rounded-xl text-sm font-semibold border-2 transition duration-200 flex flex-col items-center gap-2 group hover:shadow-md"
                style={{
                  borderColor: isSelected ? "#662498" : "#e5e7eb",
                  color: isSelected ? "#662498" : "#6b7280",
                  background: isSelected ? "#f5f0ff" : "#ffffff",
                }}
              >
                <Icon size={24} className="group-hover:scale-110 transition" />
                <span className="font-bold">{option.label}</span>
                <span className="text-xs font-normal" style={{ color: isSelected ? "#662498" : "#9ca3af" }}>
                  {option.description}
                </span>
              </button>
            );
          })}
        </div>
        <div className="mt-4 p-3 rounded-lg bg-blue-50 dark:bg-blue-900/30 border border-blue-200 dark:border-blue-800">
          <p className="text-xs text-blue-700 dark:text-blue-300">
            💡 Tip: The {theme === "system" ? "System" : theme} theme is currently active. Changes apply immediately.
          </p>
        </div>
      </section>

      <section className="bg-white dark:bg-gray-800 rounded-2xl border border-gray-100 dark:border-gray-700 shadow-sm p-6 transition duration-300">
        <h2 className="text-lg font-bold text-gray-900 dark:text-white mb-5 flex items-center gap-2">
          ⚙️ Preferences
        </h2>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div>
            <label className="block text-sm font-semibold text-gray-700 dark:text-gray-300 mb-3 flex items-center gap-2">
              <DollarSign size={18} style={{ color: "#662498" }} />
              Default Currency
            </label>
            <select
              value={currency}
              onChange={(e) => setCurrency(e.target.value)}
              className="w-full px-4 py-3 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 text-gray-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-purple-500 transition duration-200"
            >
              <option>PHP (Philippine Peso)</option>
              <option>USD (US Dollar)</option>
              <option>EUR (Euro)</option>
            </select>
          </div>
          <div>
            <label className="block text-sm font-semibold text-gray-700 dark:text-gray-300 mb-3 flex items-center gap-2">
              <Bell size={18} style={{ color: "#662498" }} />
              Notifications
            </label>
            <button
              onClick={() => setNotificationsEnabled((prev) => !prev)}
              className="w-full px-4 py-3 rounded-xl border-2 text-sm font-semibold cursor-pointer transition duration-200 hover:shadow-md"
              style={{
                borderColor: notificationsEnabled ? "#662498" : "#e5e7eb",
                color: notificationsEnabled ? "#662498" : "#6b7280",
                background: notificationsEnabled ? "#f5f0ff" : (isDark ? "#374151" : "#ffffff"),
              }}
            >
              {notificationsEnabled ? "🔔 Enabled" : "🔇 Disabled"}
            </button>
          </div>
        </div>

        {appSettingsNotice && (
          <div className="mt-4 p-3 rounded-lg bg-green-50 dark:bg-green-900/30 border border-green-200 dark:border-green-800 animate-bounce">
            <p className="text-sm text-green-700 dark:text-green-300 font-semibold">{appSettingsNotice}</p>
          </div>
        )}

        <div className="mt-6 flex gap-3">
          <button
            onClick={saveAppSettings}
            className="px-6 py-3 text-sm font-bold text-white rounded-xl transition duration-200 hover:shadow-lg transform hover:scale-105 flex items-center gap-2 cursor-pointer"
            style={{ background: "linear-gradient(135deg, #662498 0%, #a855f7 100%)" }}
          >
            <Save size={18} />
            Save Preferences
          </button>
        </div>
      </section>

      <section className="bg-white dark:bg-gray-800 rounded-2xl border border-gray-100 dark:border-gray-700 shadow-sm p-6 transition duration-300">
        <h2 className="text-lg font-bold text-gray-900 dark:text-white mb-4">ℹ️ About</h2>
        <div className="space-y-3 text-sm text-gray-600 dark:text-gray-400">
          <p><span className="font-semibold">App:</span> SplitShare</p>
          <p><span className="font-semibold">Version:</span> 1.0.0</p>
          <p><span className="font-semibold">Built with:</span> React, TypeScript & Tailwind CSS</p>
        </div>
      </section>
    </div>
  );
}
