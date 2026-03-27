import { useEffect, useState } from "react";
import { useAuth } from "../context/AuthContext";
import { GROUPS, SOCIAL } from "../data/mockData";

export default function Profile() {
  const { user } = useAuth();
  const [settingsFirstName, setSettingsFirstName] = useState("");
  const [settingsLastName, setSettingsLastName] = useState("");
  const [settingsEmail, setSettingsEmail] = useState("");
  const [settingsNotice, setSettingsNotice] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [passwordNotice, setPasswordNotice] = useState("");

  useEffect(() => {
    setSettingsFirstName(user?.firstname ?? "");
    setSettingsLastName(user?.lastname ?? "");
    setSettingsEmail(user?.email ?? "");
  }, [user]);

  const saveProfileSettings = () => {
    setSettingsNotice("Settings saved successfully.");
  };

  const updatePassword = () => {
    if (!newPassword || !confirmPassword) {
      setPasswordNotice("Please complete password fields.");
      return;
    }
    if (newPassword.length < 8) {
      setPasswordNotice("New password must be at least 8 characters.");
      return;
    }
    if (newPassword !== confirmPassword) {
      setPasswordNotice("New password and confirmation do not match.");
      return;
    }

    setPasswordNotice("Password updated successfully.");
    setNewPassword("");
    setConfirmPassword("");
  };

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-3xl font-bold text-gray-900">Profile</h1>
        <p className="text-sm text-gray-400 mt-1">Manage your account details and password</p>
      </div>

      <section className="bg-white rounded-2xl border border-gray-100 shadow-sm p-6">
        <div className="flex flex-col items-center text-center">
          <div className="w-16 h-16 rounded-full flex items-center justify-center text-lg font-bold" style={{ background: "#c9beff", color: "#4a1870" }}>
            {(user?.firstname?.[0] ?? "U").toUpperCase()}{(user?.lastname?.[0] ?? "").toUpperCase()}
          </div>
          <h2 className="mt-3 text-lg font-bold text-gray-900">{user?.firstname} {user?.lastname}</h2>
          <p className="text-xs text-gray-500">{user?.email}</p>
          <div className="mt-5 grid grid-cols-3 gap-6">
            <div>
              <p className="text-lg font-bold text-gray-900">{GROUPS.length}</p>
              <p className="text-xs text-gray-500 uppercase tracking-wide">Groups</p>
            </div>
            <div>
              <p className="text-lg font-bold text-gray-900">{SOCIAL.followers}</p>
              <p className="text-xs text-gray-500 uppercase tracking-wide">Followers</p>
            </div>
            <div>
              <p className="text-lg font-bold text-gray-900">{SOCIAL.following}</p>
              <p className="text-xs text-gray-500 uppercase tracking-wide">Following</p>
            </div>
          </div>
        </div>
      </section>

      <section className="bg-white rounded-2xl border border-gray-100 shadow-sm p-6">
        <h2 className="text-base font-bold text-gray-800 mb-4">Account Settings</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label className="block text-xs font-semibold tracking-wide uppercase text-gray-500 mb-2">First name</label>
            <input value={settingsFirstName} onChange={(e) => setSettingsFirstName(e.target.value)} className="w-full px-3 py-2.5 rounded-xl border border-gray-200 focus:outline-none focus:ring-2 focus:ring-purple-200" />
          </div>
          <div>
            <label className="block text-xs font-semibold tracking-wide uppercase text-gray-500 mb-2">Last name</label>
            <input value={settingsLastName} onChange={(e) => setSettingsLastName(e.target.value)} className="w-full px-3 py-2.5 rounded-xl border border-gray-200 focus:outline-none focus:ring-2 focus:ring-purple-200" />
          </div>
          <div className="md:col-span-2">
            <label className="block text-xs font-semibold tracking-wide uppercase text-gray-500 mb-2">Email</label>
            <input value={settingsEmail} onChange={(e) => setSettingsEmail(e.target.value)} className="w-full px-3 py-2.5 rounded-xl border border-gray-200 focus:outline-none focus:ring-2 focus:ring-purple-200" />
          </div>
          <div>
            <label className="block text-xs font-semibold tracking-wide uppercase text-gray-500 mb-2">New password</label>
            <input type="password" value={newPassword} onChange={(e) => setNewPassword(e.target.value)} className="w-full px-3 py-2.5 rounded-xl border border-gray-200 focus:outline-none focus:ring-2 focus:ring-purple-200" />
          </div>
          <div>
            <label className="block text-xs font-semibold tracking-wide uppercase text-gray-500 mb-2">Confirm password</label>
            <input type="password" value={confirmPassword} onChange={(e) => setConfirmPassword(e.target.value)} className="w-full px-3 py-2.5 rounded-xl border border-gray-200 focus:outline-none focus:ring-2 focus:ring-purple-200" />
          </div>
        </div>
        {settingsNotice && <p className="text-sm text-green-600 mt-4">{settingsNotice}</p>}
        {passwordNotice && <p className="text-sm text-gray-600 mt-2">{passwordNotice}</p>}
        <div className="mt-5 flex gap-3">
          <button onClick={saveProfileSettings} className="px-5 py-2.5 text-sm font-bold text-white rounded-xl transition cursor-pointer" style={{ background: "#662498" }}>Save changes</button>
          <button onClick={updatePassword} className="px-5 py-2.5 text-sm font-bold text-white rounded-xl transition cursor-pointer" style={{ background: "#662498" }}>Update password</button>
        </div>
      </section>
    </div>
  );
}
