import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Lock } from "lucide-react";
import { useAuth } from "../../auth/AuthContext";
import { userApi } from "../services/userService";
import type { UserConnectionDto, UserProfileStatsDto } from "../types/social";

export default function ProfilePage() {
  const navigate = useNavigate();
  const { user, login, token, logout } = useAuth();
  const [settingsFirstName, setSettingsFirstName] = useState("");
  const [settingsLastName, setSettingsLastName] = useState("");
  const [settingsEmail, setSettingsEmail] = useState("");
  const [settingsNotice, setSettingsNotice] = useState("");
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [passwordNotice, setPasswordNotice] = useState("");
  const [showEmailChangeModal, setShowEmailChangeModal] = useState(false);
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

  useEffect(() => {
    userApi.getProfileStats()
      .then((response) => setStats(response.data.data))
      .catch((error) => console.error("Failed to fetch profile stats", error));
  }, []);

  const loadFollowers = async () => {
    const response = await userApi.getFollowers();
    setFollowers(response.data.data ?? []);
  };

  const loadFollowing = async () => {
    const response = await userApi.getFollowing();
    setFollowing(response.data.data ?? []);
  };

  const submitProfileSettings = async (skipEmailWarning = false) => {
    try {
      const emailChanged = (user?.email ?? "") !== settingsEmail.trim();
      if (emailChanged && !skipEmailWarning) {
        setShowEmailChangeModal(true);
        return;
      }

      const response = await userApi.updateProfile(settingsFirstName, settingsLastName, settingsEmail);
      if (response.data.success && response.data.data && token) {
        const updatedUser = response.data.data;
        if (emailChanged) {
          setShowEmailChangeModal(false);
          logout();
          navigate("/login", { replace: true });
          return;
        }

        const refreshToken = localStorage.getItem("refreshToken") || "";
        login(updatedUser, token, refreshToken);
        setSettingsNotice("Settings saved successfully.");
        setTimeout(() => setSettingsNotice(""), 3000);
      }
    } catch (error) {
      console.error("Failed to update profile", error);
      setSettingsNotice("Failed to save settings. Please try again.");
    }
  };

  const saveProfileSettings = () => {
    void submitProfileSettings();
  };

  const confirmEmailChange = () => {
    void submitProfileSettings(true);
  };

  const updatePassword = async () => {
    if (!currentPassword || !newPassword || !confirmPassword) {
      setPasswordNotice("Please complete all password fields.");
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

    try {
      await userApi.updatePassword(currentPassword, newPassword);
      setPasswordNotice("Password updated successfully.");
      setCurrentPassword("");
      setNewPassword("");
      setConfirmPassword("");
      setTimeout(() => setPasswordNotice(""), 3000);
    } catch (error) {
      console.error("Failed to update password", error);
      setPasswordNotice("Failed to update password. Please check your current password and try again.");
    }
  };

  const openConnections = async (mode: "followers" | "following") => {
    setConnectionsLoading(true);
    setConnectionsError("");
    if (mode === "followers") {
      setShowFollowersModal(true);
    } else {
      setShowFollowingModal(true);
    }

    try {
      if (mode === "followers") {
        await loadFollowers();
      } else {
        await loadFollowing();
      }
    } catch {
      setConnectionsError(mode === "followers" ? "Unable to load followers." : "Unable to load following users.");
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

      await Promise.all([
        userApi.getProfileStats().then((response) => setStats(response.data.data)),
        loadFollowers(),
        loadFollowing(),
      ]);
    } catch {
      setConnectionsError("Unable to update follow status.");
    } finally {
      setConnectionActionUserId(null);
    }
  };

  const activeConnections = showFollowersModal ? followers : following;
  const modalTitle = showFollowersModal ? "Followers" : "Following";

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-4xl font-bold text-gray-900 dark:text-white">Profile</h1>
        <p className="mt-2 text-sm text-gray-500 dark:text-gray-400">Manage your account details and password</p>
      </div>

      <section className="rounded-2xl border border-gray-100 dark:border-gray-700 bg-white dark:bg-gray-800 p-6 shadow-sm transition duration-300">
        <div className="flex flex-col items-center text-center">
          <div className="flex h-16 w-16 items-center justify-center rounded-full text-lg font-bold text-white" style={{ background: "linear-gradient(135deg, #662498 0%, #a855f7 100%)" }}>
            {(user?.firstname?.[0] ?? "U").toUpperCase()}{(user?.lastname?.[0] ?? "").toUpperCase()}
          </div>
          <h2 className="mt-3 text-lg font-bold text-gray-900 dark:text-white">{user?.firstname} {user?.lastname}</h2>
          <p className="text-xs text-gray-500 dark:text-gray-400">{user?.email}</p>

          <div className="mt-5 grid grid-cols-3 gap-6">
            <button type="button" onClick={() => navigate("/groups")} className="rounded-xl p-2 transition hover:bg-gray-50 dark:hover:bg-gray-700">
              <p className="text-lg font-bold text-gray-900 dark:text-white">{stats?.groupsCount ?? 0}</p>
              <p className="text-xs uppercase tracking-wide text-gray-500 dark:text-gray-400">Groups</p>
            </button>
            <button type="button" onClick={() => openConnections("followers")} className="rounded-xl p-2 transition hover:bg-gray-50 dark:hover:bg-gray-700">
              <p className="text-lg font-bold text-gray-900 dark:text-white">{stats?.followersCount ?? 0}</p>
              <p className="text-xs uppercase tracking-wide text-gray-500 dark:text-gray-400">Followers</p>
            </button>
            <button type="button" onClick={() => openConnections("following")} className="rounded-xl p-2 transition hover:bg-gray-50 dark:hover:bg-gray-700">
              <p className="text-lg font-bold text-gray-900 dark:text-white">{stats?.followingCount ?? 0}</p>
              <p className="text-xs uppercase tracking-wide text-gray-500 dark:text-gray-400">Following</p>
            </button>
          </div>
        </div>
      </section>

      <section className="rounded-2xl border border-gray-100 dark:border-gray-700 bg-white dark:bg-gray-800 p-6 shadow-sm transition duration-300">
        <h2 className="mb-4 text-lg font-bold text-gray-800 dark:text-white">Account Settings</h2>
        <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
          <div>
            <label className="mb-2 block text-xs font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400">First name</label>
            <input value={settingsFirstName} onChange={(e) => setSettingsFirstName(e.target.value)} className="w-full rounded-xl border border-gray-200 dark:border-gray-600 bg-white dark:bg-gray-700 px-3 py-2.5 text-gray-900 dark:text-white transition focus:outline-none focus:ring-2 focus:ring-purple-500" />
          </div>
          <div>
            <label className="mb-2 block text-xs font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400">Last name</label>
            <input value={settingsLastName} onChange={(e) => setSettingsLastName(e.target.value)} className="w-full rounded-xl border border-gray-200 dark:border-gray-600 bg-white dark:bg-gray-700 px-3 py-2.5 text-gray-900 dark:text-white transition focus:outline-none focus:ring-2 focus:ring-purple-500" />
          </div>
          <div className="md:col-span-2">
            <label className="mb-2 block text-xs font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400">Email</label>
            <input value={settingsEmail} onChange={(e) => setSettingsEmail(e.target.value)} className="w-full rounded-xl border border-gray-200 dark:border-gray-600 bg-white dark:bg-gray-700 px-3 py-2.5 text-gray-900 dark:text-white transition focus:outline-none focus:ring-2 focus:ring-purple-500" />
          </div>
          <div>
            <label className="mb-2 block text-xs font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400">Current password</label>
            <input type="password" value={currentPassword} onChange={(e) => setCurrentPassword(e.target.value)} className="w-full rounded-xl border border-gray-200 dark:border-gray-600 bg-white dark:bg-gray-700 px-3 py-2.5 text-gray-900 dark:text-white transition focus:outline-none focus:ring-2 focus:ring-purple-500" />
          </div>
          <div>
            <label className="mb-2 block text-xs font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400">New password</label>
            <input type="password" value={newPassword} onChange={(e) => setNewPassword(e.target.value)} className="w-full rounded-xl border border-gray-200 dark:border-gray-600 bg-white dark:bg-gray-700 px-3 py-2.5 text-gray-900 dark:text-white transition focus:outline-none focus:ring-2 focus:ring-purple-500" />
          </div>
          <div>
            <label className="mb-2 block text-xs font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400">Confirm password</label>
            <input type="password" value={confirmPassword} onChange={(e) => setConfirmPassword(e.target.value)} className="w-full rounded-xl border border-gray-200 dark:border-gray-600 bg-white dark:bg-gray-700 px-3 py-2.5 text-gray-900 dark:text-white transition focus:outline-none focus:ring-2 focus:ring-purple-500" />
          </div>
        </div>
        {settingsNotice && <p className="mt-4 text-sm text-green-600 dark:text-green-400">{settingsNotice}</p>}
        {passwordNotice && <p className="mt-2 text-sm text-gray-600 dark:text-gray-400">{passwordNotice}</p>}
        <div className="mt-5 flex gap-3">
          <button onClick={saveProfileSettings} className="rounded-xl px-5 py-2.5 text-sm font-bold text-white shadow-sm transition hover:shadow-lg" style={{ background: "linear-gradient(135deg, #662498 0%, #a855f7 100%)" }}>
            Save changes
          </button>
          <button onClick={updatePassword} className="flex items-center gap-2 rounded-xl px-5 py-2.5 text-sm font-bold text-white shadow-sm transition hover:shadow-lg" style={{ background: "linear-gradient(135deg, #662498 0%, #a855f7 100%)" }}>
            <Lock size={16} />
            Update password
          </button>
        </div>
      </section>

      {showEmailChangeModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 px-4" onClick={() => setShowEmailChangeModal(false)}>
          <div className="w-full max-w-md rounded-2xl border border-gray-100 dark:border-gray-700 bg-white dark:bg-gray-800 p-6 shadow-2xl" onClick={(event) => event.stopPropagation()}>
            <h3 className="text-xl font-bold text-gray-900 dark:text-white">Change email address?</h3>
            <p className="mt-2 text-sm leading-6 text-gray-600 dark:text-gray-300">
              Updating your email will sign you out after the change is saved. You will need to sign in again with the new email address.
            </p>

            <div className="mt-6 flex gap-3">
              <button
                type="button"
                onClick={() => setShowEmailChangeModal(false)}
                className="flex-1 rounded-xl border border-gray-200 dark:border-gray-700 px-4 py-3 text-sm font-semibold text-gray-700 dark:text-gray-300 transition hover:bg-gray-50 dark:hover:bg-gray-700 cursor-pointer"
              >
                Cancel
              </button>
              <button
                type="button"
                onClick={confirmEmailChange}
                className="flex-1 rounded-xl px-4 py-3 text-sm font-bold text-white shadow-sm transition hover:shadow-lg cursor-pointer"
                style={{ background: "linear-gradient(135deg, #662498 0%, #a855f7 100%)" }}
              >
                OK, continue
              </button>
            </div>
          </div>
        </div>
      )}

      {(showFollowersModal || showFollowingModal) && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4" onClick={closeConnectionsModals}>
          <div className="w-full max-w-lg rounded-2xl bg-white dark:bg-gray-800 p-6 shadow-2xl transition duration-300" onClick={(event) => event.stopPropagation()}>
            <div className="mb-4 flex items-center justify-between">
              <h3 className="text-lg font-bold text-gray-900 dark:text-white">{modalTitle}</h3>
              <button type="button" onClick={closeConnectionsModals} className="rounded-lg border border-gray-200 dark:border-gray-600 px-3 py-1 text-xs font-semibold text-gray-700 dark:text-gray-300 transition hover:bg-gray-50 dark:hover:bg-gray-700">
                Close
              </button>
            </div>

            {connectionsLoading ? (
              <div className="flex items-center justify-center py-10">
                <div className="h-8 w-8 animate-spin rounded-full border-4 border-purple-500/20 border-t-purple-600" />
              </div>
            ) : connectionsError ? (
              <div className="rounded-xl border border-red-200 dark:border-red-800 bg-red-50 dark:bg-red-900/30 px-4 py-3 text-sm text-red-700 dark:text-red-300">{connectionsError}</div>
            ) : (
              <div className="max-h-80 space-y-2 overflow-y-auto pr-1">
                {activeConnections.length === 0 ? (
                  <p className="py-6 text-center text-sm text-gray-500 dark:text-gray-400">No users found.</p>
                ) : (
                  activeConnections.map((person) => (
                    <div key={person.id} className="rounded-xl border border-gray-100 dark:border-gray-700 bg-white dark:bg-gray-700 px-4 py-3 transition">
                      <div className="flex items-center justify-between gap-3">
                        <div>
                          <p className="text-sm font-semibold text-gray-900 dark:text-white">{person.firstname} {person.lastname}</p>
                          <p className="text-xs text-gray-500 dark:text-gray-400">{person.email}</p>
                        </div>
                        <button
                          type="button"
                          onClick={() => toggleConnectionFollow(person)}
                          disabled={connectionActionUserId === person.id}
                          className={`rounded-lg px-3 py-1.5 text-xs font-semibold transition ${person.following ? "border border-red-200 dark:border-red-800 text-red-700 dark:text-red-300 hover:bg-red-50 dark:hover:bg-red-900/30" : "border border-purple-200 dark:border-purple-800 text-purple-700 dark:text-purple-300 hover:bg-purple-50 dark:hover:bg-purple-900/30"}`}
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