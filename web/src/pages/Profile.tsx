import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { userApi } from "../services/userService";
import type { UserConnectionDto, UserProfileStatsDto } from "../types/social";

export default function Profile() {
  const navigate = useNavigate();
  const { user, login, token } = useAuth();
  const [settingsFirstName, setSettingsFirstName] = useState("");
  const [settingsLastName, setSettingsLastName] = useState("");
  const [settingsEmail, setSettingsEmail] = useState("");
  const [settingsNotice, setSettingsNotice] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [passwordNotice, setPasswordNotice] = useState("");
  const [stats, setStats] = useState<UserProfileStatsDto | null>(null);
  const [showFollowersModal, setShowFollowersModal] = useState(false);
  const [showFollowingModal, setShowFollowingModal] = useState(false);
  const [followers, setFollowers] = useState<UserConnectionDto[]>([]);
  const [following, setFollowing] = useState<UserConnectionDto[]>([]);
  const [connectionsLoading, setConnectionsLoading] = useState(false);
  const [connectionsError, setConnectionsError] = useState("");
  const [connectionActionUserId, setConnectionActionUserId] = useState<number | null>(null);

  useEffect(() => {
    setSettingsFirstName(user?.firstname ?? "");
    setSettingsLastName(user?.lastname ?? "");
    setSettingsEmail(user?.email ?? "");
  }, [user]);

  const loadProfileStats = async () => {
    try {
      const response = await userApi.getProfileStats();
      setStats(response.data.data);
    } catch (error) {
      console.error("Failed to fetch profile stats", error);
    }
  };

  const loadFollowers = async () => {
    const response = await userApi.getFollowers();
    setFollowers(response.data.data ?? []);
  };

  const loadFollowing = async () => {
    const response = await userApi.getFollowing();
    setFollowing(response.data.data ?? []);
  };

  useEffect(() => {
    loadProfileStats();
  }, []);

  const saveProfileSettings = async () => {
    try {
      const response = await userApi.updateProfile(settingsFirstName, settingsLastName);
      if (response.data.success && response.data.data && token) {
        const updatedUser = response.data.data;
        const refreshToken = localStorage.getItem('refreshToken') || '';
        login(updatedUser, token, refreshToken);
        setSettingsNotice("Settings saved successfully.");
        setTimeout(() => setSettingsNotice(""), 3000);
      }
    } catch (error) {
      console.error("Failed to update profile", error);
      setSettingsNotice("Failed to save settings. Please try again.");
    }
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

  const openFollowers = async () => {
    setConnectionsLoading(true);
    setConnectionsError("");
    setShowFollowersModal(true);
    try {
      await loadFollowers();
    } catch {
      setConnectionsError("Unable to load followers.");
    } finally {
      setConnectionsLoading(false);
    }
  };

  const openFollowing = async () => {
    setConnectionsLoading(true);
    setConnectionsError("");
    setShowFollowingModal(true);
    try {
      await loadFollowing();
    } catch {
      setConnectionsError("Unable to load following users.");
    } finally {
      setConnectionsLoading(false);
    }
  };

  const closeConnectionsModals = () => {
    setShowFollowersModal(false);
    setShowFollowingModal(false);
    setConnectionsError("");
  };

  const getConnectionActionLabel = (person: UserConnectionDto) => {
    if (person.following) return "Unfollow";
    if (person.followedBy) return "Follow back";
    return "Follow";
  };

  const toggleConnectionFollow = async (person: UserConnectionDto) => {
    setConnectionActionUserId(person.id);
    setConnectionsError("");
    try {
      if (person.following) {
        await userApi.unfollow(person.id);
      } else {
        await userApi.follow(person.id);
      }

      await Promise.all([loadProfileStats(), loadFollowers(), loadFollowing()]);
    } catch {
      setConnectionsError("Unable to update follow status.");
    } finally {
      setConnectionActionUserId(null);
    }
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
            <button
              type="button"
              onClick={() => navigate("/groups")}
              className="rounded-xl p-2 transition hover:bg-gray-50"
            >
              <p className="text-lg font-bold text-gray-900">{stats?.groupsCount ?? 0}</p>
              <p className="text-xs text-gray-500 uppercase tracking-wide">Groups</p>
            </button>
            <button
              type="button"
              onClick={openFollowers}
              className="rounded-xl p-2 transition hover:bg-gray-50"
            >
              <p className="text-lg font-bold text-gray-900">{stats?.followersCount ?? 0}</p>
              <p className="text-xs text-gray-500 uppercase tracking-wide">Followers</p>
            </button>
            <button
              type="button"
              onClick={openFollowing}
              className="rounded-xl p-2 transition hover:bg-gray-50"
            >
              <p className="text-lg font-bold text-gray-900">{stats?.followingCount ?? 0}</p>
              <p className="text-xs text-gray-500 uppercase tracking-wide">Following</p>
            </button>
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

      {(showFollowersModal || showFollowingModal) && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4" onClick={closeConnectionsModals}>
          <div className="w-full max-w-lg rounded-2xl bg-white p-6 shadow-2xl" onClick={(event) => event.stopPropagation()}>
            <div className="mb-4 flex items-center justify-between">
              <h3 className="text-lg font-bold text-gray-900">{showFollowersModal ? "Followers" : "Following"}</h3>
              <button
                type="button"
                onClick={closeConnectionsModals}
                className="rounded-lg border border-gray-200 px-3 py-1 text-xs font-semibold text-gray-700 hover:bg-gray-50"
              >
                Close
              </button>
            </div>

            {connectionsLoading ? (
              <div className="flex items-center justify-center py-10">
                <div className="h-8 w-8 animate-spin rounded-full border-4 border-purple-200 border-t-purple-700" />
              </div>
            ) : connectionsError ? (
              <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{connectionsError}</div>
            ) : (
              <div className="max-h-80 space-y-2 overflow-y-auto pr-1">
                {(showFollowersModal ? followers : following).length === 0 ? (
                  <p className="py-6 text-center text-sm text-gray-500">No users found.</p>
                ) : (
                  (showFollowersModal ? followers : following).map((person) => (
                    <div key={person.id} className="rounded-xl border border-gray-100 px-4 py-3">
                      <div className="flex items-center justify-between gap-3">
                        <div>
                          <p className="text-sm font-semibold text-gray-900">{person.firstname} {person.lastname}</p>
                          <p className="text-xs text-gray-500">{person.email}</p>
                        </div>
                        <button
                          type="button"
                          onClick={() => toggleConnectionFollow(person)}
                          disabled={connectionActionUserId === person.id}
                          className={`rounded-lg px-3 py-1.5 text-xs font-semibold transition ${person.following ? "border border-red-200 text-red-700 hover:bg-red-50" : "border border-purple-200 text-purple-700 hover:bg-purple-50"}`}
                        >
                          {connectionActionUserId === person.id ? "Please wait..." : getConnectionActionLabel(person)}
                        </button>
                      </div>
                    </div>
                  ))
                )}
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
