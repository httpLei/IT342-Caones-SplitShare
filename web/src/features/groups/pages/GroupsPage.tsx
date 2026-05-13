import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Plus, Search, UserPlus, Users, TrendingDown, TrendingUp } from "lucide-react";
import CreateGroupModal from "../components/CreateGroupModal";
import { useTheme } from "../../settings/contexts/ThemeContext";
import { useCurrency } from "../../settings/contexts/CurrencyContext";
import { groupApi } from "../services/groupService";
import { userApi } from "../../profile/services/userService";
import type { GroupSummaryDto } from "../types/groups";
import type { UserConnectionDto } from "../../profile/types/social";
import { formatCurrency, signedCurrency } from "../../../shared/utils/format";

export default function GroupsPage() {
  const navigate = useNavigate();
  const { isDark } = useTheme();
  const { currency } = useCurrency();
  const [connectionQuery, setConnectionQuery] = useState("");
  const [connectionResults, setConnectionResults] = useState<UserConnectionDto[]>([]);
  const [connectionLoading, setConnectionLoading] = useState(false);
  const [connectionError, setConnectionError] = useState("");
  const [groupSearch, setGroupSearch] = useState("");
  const [groups, setGroups] = useState<GroupSummaryDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [modalOpen, setModalOpen] = useState(false);
  const [saving, setSaving] = useState(false);
  const [saveError, setSaveError] = useState("");
  const [mutuals, setMutuals] = useState<UserConnectionDto[]>([]);

  const getErrorMessage = (err: unknown, fallback: string) => {
    const message = (err as { response?: { data?: { error?: { message?: string } } } })?.response?.data?.error?.message;
    return message ?? fallback;
  };

  const loadMutuals = async () => {
    const response = await userApi.getMutuals();
    setMutuals(response.data.data ?? []);
  };

  const loadGroups = () => {
    setError("");
    setLoading(true);
    groupApi.getGroups()
      .then((response) => setGroups(response.data.data ?? []))
      .catch((err) => setError(err?.response?.data?.error?.message ?? "Unable to load groups."))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadGroups();
    loadMutuals().catch(() => {
      setConnectionError("Unable to load mutual connections.");
    });
  }, []);

  useEffect(() => {
    const keyword = connectionQuery.trim();

    if (!keyword) {
      setConnectionResults([]);
      setConnectionError("");
      setConnectionLoading(false);
      return;
    }

    setConnectionLoading(true);
    setConnectionError("");

    const timer = window.setTimeout(() => {
      userApi.search(keyword)
        .then((response) => setConnectionResults(response.data.data ?? []))
        .catch((err: unknown) => setConnectionError(getErrorMessage(err, "Unable to search users.")))
        .finally(() => setConnectionLoading(false));
    }, 300);

    return () => window.clearTimeout(timer);
  }, [connectionQuery]);

  const filteredGroups = useMemo(() => {
    const keyword = groupSearch.trim().toLowerCase();
    if (!keyword) return groups;
    return groups.filter((group) => group.name.toLowerCase().includes(keyword) || group.members.join(" ").toLowerCase().includes(keyword));
  }, [groupSearch, groups]);

  const handleCreateGroup = async (payload: { name: string; memberEmails: string[] }) => {
    setSaving(true);
    setSaveError("");
    try {
      const response = await groupApi.createGroup(payload);
      const created = response.data.data;
      setModalOpen(false);
      loadGroups();
      await loadMutuals();
      if (created?.id) {
        navigate(`/groups/${created.id}`);
      }
    } catch (err: unknown) {
      setSaveError((err as { response?: { data?: { error?: { message?: string } } } })?.response?.data?.error?.message ?? "Unable to create group.");
    } finally {
      setSaving(false);
    }
  };

  const handleSearchUsers = async (keyword = connectionQuery.trim()) => {
    setConnectionError("");

    if (!keyword) {
      setConnectionResults([]);
      return;
    }

    setConnectionLoading(true);
    try {
      const response = await userApi.search(keyword);
      setConnectionResults(response.data.data ?? []);
    } catch (err: unknown) {
      setConnectionError(getErrorMessage(err, "Unable to search users."));
    } finally {
      setConnectionLoading(false);
    }
  };

  const toggleFollow = async (user: UserConnectionDto) => {
    setConnectionError("");
    try {
      if (user.following) {
        await userApi.unfollow(user.id);
      } else {
        await userApi.follow(user.id);
      }

      await Promise.all([handleSearchUsers(), loadMutuals()]);
    } catch (err: unknown) {
      setConnectionError(getErrorMessage(err, "Unable to update follow status."));
    }
  };

  return (
    <div className="space-y-8">
      <div className="flex items-start justify-between gap-4">
        <div>
          <h1 className="text-4xl font-bold text-gray-900 dark:text-white">Groups 👥</h1>
          <p className="text-sm text-gray-500 dark:text-gray-400 mt-2">Manage your shared expense groups</p>
        </div>
        <button
          onClick={() => setModalOpen(true)}
          className="flex items-center gap-2 px-6 py-3 text-sm font-bold text-white rounded-xl transition duration-200 hover:shadow-lg transform hover:scale-105 cursor-pointer"
          style={{ background: "linear-gradient(135deg, #662498 0%, #a855f7 100%)" }}
        >
          <Plus size={20} />
          New Group
        </button>
      </div>

      {error && <div className="rounded-2xl border border-red-200 bg-red-50 dark:bg-red-900/30 dark:border-red-800 px-4 py-3 text-sm text-red-700 dark:text-red-300 flex items-center gap-2"><span>❌</span> {error}</div>}
      {connectionError && <div className="rounded-2xl border border-red-200 bg-red-50 dark:bg-red-900/30 dark:border-red-800 px-4 py-3 text-sm text-red-700 dark:text-red-300 flex items-center gap-2"><span>❌</span> {connectionError}</div>}

      <div className="bg-white dark:bg-gray-800 rounded-2xl border border-gray-100 dark:border-gray-700 shadow-sm p-6">
        <div>
          <h2 className="text-lg font-bold text-gray-900 dark:text-white mb-4 flex items-center gap-2">
            <UserPlus size={20} style={{ color: "#662498" }} />
            Find users
          </h2>
          <div className="flex gap-3">
            <div className="flex-1 relative">
              <Search size={18} className="absolute left-4 top-3.5 text-gray-400 dark:text-gray-500" />
              <input
                value={connectionQuery}
                onChange={(event) => setConnectionQuery(event.target.value)}
                onKeyDown={(event) => event.key === "Enter" && handleSearchUsers()}
                placeholder="Search by name or email"
                className="w-full pl-12 pr-4 py-2.5 text-sm border border-gray-200 dark:border-gray-700 dark:bg-gray-900 dark:text-white rounded-xl focus:outline-none focus:ring-2 focus:ring-purple-200"
              />
            </div>
            <button
              onClick={() => handleSearchUsers()}
              className="rounded-xl px-6 py-2.5 text-sm font-bold text-white transition duration-200 cursor-pointer hover:shadow-md"
              style={{ background: "#662498" }}
            >
              Search
            </button>
          </div>

          <div className="mt-4 space-y-2 max-h-48 overflow-y-auto">
            {connectionLoading ? (
              <p className="text-sm text-gray-500 dark:text-gray-400 text-center py-4">🔍 Searching users...</p>
            ) : connectionResults.length === 0 ? (
              <></>
            ) : (
              connectionResults.map((user) => (
                <div key={user.id} className="rounded-xl border border-gray-100 dark:border-gray-700 dark:bg-gray-900 px-4 py-3 flex items-center justify-between gap-3 hover:bg-gray-50 dark:hover:bg-gray-800 transition">
                  <div className="flex items-center gap-3 flex-1">
                    <div className="w-10 h-10 rounded-lg bg-gradient-to-br from-purple-400 to-pink-400 flex items-center justify-center text-white font-bold text-sm">
                      {user.firstname.charAt(0)}{user.lastname.charAt(0)}
                    </div>
                    <div className="flex-1">
                      <p className="text-sm font-semibold text-gray-800 dark:text-gray-200">{user.firstname} {user.lastname}</p>
                      <p className="text-xs text-gray-500 dark:text-gray-400">{user.email}</p>
                      <p className="text-xs mt-1" style={{ color: user.mutual ? "#16a34a" : "#6b7280" }}>
                        {user.mutual ? "✓ Mutual connection" : user.following ? "⏳ Awaiting follow back" : user.followedBy ? "→ Follows you" : "→ Not followed"}
                      </p>
                    </div>
                  </div>
                  <button
                    onClick={() => toggleFollow(user)}
                    className="rounded-lg px-4 py-2 text-xs font-bold transition duration-200 cursor-pointer flex items-center gap-1"
                    style={{
                      background: user.following ? (isDark ? "#374151" : "#e5e7eb") : "#662498",
                      color: user.following ? (isDark ? "#e5e7eb" : "#374151") : "#ffffff",
                    }}
                  >
                    {user.following ? "Unfollow" : "Follow"}
                  </button>
                </div>
              ))
            )}
          </div>
        </div>

        <div className="my-6 border-t border-gray-100 dark:border-gray-700" />

        <div>
          <h2 className="text-lg font-bold text-gray-900 dark:text-white mb-4 flex items-center gap-2">
            <Users size={20} style={{ color: "#662498" }} />
            Your Groups
          </h2>
          <div className="relative mb-4">
            <Search size={18} className="absolute left-4 top-3.5 text-gray-400 dark:text-gray-500" />
            <input
              value={groupSearch}
              onChange={(e) => setGroupSearch(e.target.value)}
              placeholder="Search groups by name..."
              className="w-full pl-12 pr-4 py-2.5 text-sm border border-gray-200 dark:border-gray-700 dark:bg-gray-900 dark:text-white rounded-xl focus:outline-none focus:ring-2 focus:ring-purple-200"
            />
          </div>

          {loading ? (
            <div className="rounded-xl border border-dashed border-gray-300 dark:border-gray-600 px-4 py-12 text-center">
              <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-purple-600"></div>
              <p className="text-sm text-gray-500 dark:text-gray-400 mt-3">Loading groups...</p>
            </div>
          ) : (
            <div className="space-y-3">
            {filteredGroups.length === 0 ? (
              <div className="rounded-xl border border-dashed border-gray-300 dark:border-gray-600 px-4 py-8 text-center text-sm text-gray-500 dark:text-gray-400">
                <span className="text-2xl">📭</span>
                <p className="mt-2">No groups found.</p>
              </div>
            ) : filteredGroups.map((group) => (
              <button
                key={group.id}
                onClick={() => navigate(`/groups/${group.id}`)}
                className="w-full rounded-xl border border-gray-100 dark:border-gray-700 shadow-sm bg-white dark:bg-gray-800 px-4 py-4 flex items-center justify-between gap-4 text-left cursor-pointer hover:border-purple-300 dark:hover:border-purple-500 hover:shadow-md transition duration-200"
              >
                <div className="flex items-center gap-4 flex-1">
                  <div className="w-12 h-12 rounded-lg flex items-center justify-center text-xl" style={{ background: isDark ? "#4c1d95" : "#f3e8ff" }}>
                    👥
                  </div>
                  <div>
                    <p className="text-lg font-bold text-gray-900 dark:text-white">{group.name}</p>
                    <p className="text-xs text-gray-500 dark:text-gray-400 mt-0.5 flex items-center gap-1">
                      <Users size={14} /> {group.members.length} member{group.members.length !== 1 ? 's' : ''}
                    </p>
                    <p className="text-xs text-gray-400 dark:text-gray-500 mt-1">Total: {formatCurrency(group.total, currency)}</p>
                  </div>
                </div>
                <div className="text-right">
                  <div className="flex items-center gap-1 justify-end">
                    {group.balance >= 0 ? <TrendingUp size={16} style={{ color: "#16a34a" }} /> : <TrendingDown size={16} style={{ color: "#dc2626" }} />}
                    <p className="text-xl font-bold" style={{ color: group.balance >= 0 ? "#16a34a" : "#dc2626" }}>{signedCurrency(group.balance, currency)}</p>
                  </div>
                  <p className="text-xs font-medium mt-0.5" style={{ color: group.balance >= 0 ? "#16a34a" : "#dc2626" }}>
                    {group.balance >= 0 ? "owed to you" : "you owe"}
                  </p>
                </div>
              </button>
            ))}
            </div>
          )}
        </div>
      </div>

      <CreateGroupModal
        open={modalOpen}
        loading={saving}
        error={saveError}
        mutuals={mutuals}
        onClose={() => setModalOpen(false)}
        onSubmit={handleCreateGroup}
      />
    </div>
  );
}
