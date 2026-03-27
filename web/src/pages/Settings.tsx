import { useState } from "react";

export default function Settings() {
  const [currency, setCurrency] = useState("PHP (Philippine Peso)");
  const [theme, setTheme] = useState("System");
  const [notificationsEnabled, setNotificationsEnabled] = useState(true);
  const [appSettingsNotice, setAppSettingsNotice] = useState("");

  const saveAppSettings = () => {
    setAppSettingsNotice("App preferences updated.");
  };

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-3xl font-bold text-gray-900">Settings</h1>
        <p className="text-sm text-gray-400 mt-1">Customize your app experience</p>
      </div>

      <section className="bg-white rounded-2xl border border-gray-100 shadow-sm p-6">
        <h2 className="text-base font-bold text-gray-800 mb-4">Appearance</h2>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
          {["Light", "Dark", "System"].map((item) => {
            const selected = theme === item;
            return (
              <button
                key={item}
                onClick={() => setTheme(item)}
                className="px-4 py-3 rounded-xl text-sm font-semibold border transition cursor-pointer"
                style={{
                  borderColor: selected ? "#c4b5fd" : "#e5e7eb",
                  color: selected ? "#5b21b6" : "#6b7280",
                  background: selected ? "#f5f3ff" : "#ffffff"
                }}
              >
                {item}
              </button>
            );
          })}
        </div>
      </section>

      <section className="bg-white rounded-2xl border border-gray-100 shadow-sm p-6">
        <h2 className="text-base font-bold text-gray-800 mb-4">Preferences</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label className="block text-xs font-semibold tracking-wide uppercase text-gray-500 mb-2">Default currency</label>
            <select value={currency} onChange={(e) => setCurrency(e.target.value)} className="w-full px-3 py-2.5 rounded-xl border border-gray-200 bg-white focus:outline-none focus:ring-2 focus:ring-purple-200">
              <option>PHP (Philippine Peso)</option>
              <option>USD (US Dollar)</option>
              <option>EUR (Euro)</option>
            </select>
          </div>
          <div>
            <label className="block text-xs font-semibold tracking-wide uppercase text-gray-500 mb-2">Notifications</label>
            <button
              onClick={() => setNotificationsEnabled((prev) => !prev)}
              className="w-full px-3 py-2.5 rounded-xl border text-sm font-semibold cursor-pointer"
              style={{
                borderColor: notificationsEnabled ? "#c4b5fd" : "#e5e7eb",
                color: notificationsEnabled ? "#5b21b6" : "#6b7280",
                background: notificationsEnabled ? "#f5f3ff" : "#ffffff"
              }}
            >
              {notificationsEnabled ? "Enabled" : "Disabled"}
            </button>
          </div>
        </div>
        {appSettingsNotice && <p className="text-sm text-green-600 mt-4">{appSettingsNotice}</p>}
        <div className="mt-5">
          <button onClick={saveAppSettings} className="px-5 py-2.5 text-sm font-bold text-white rounded-xl transition cursor-pointer" style={{ background: "#662498" }}>
            Save preferences
          </button>
        </div>
      </section>
    </div>
  );
}
