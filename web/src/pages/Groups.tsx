import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import CreateGroupModal from "../components/CreateGroupModal";
import { groupApi } from "../services/groupService";
import { userApi } from "../services/userService";
import type { GroupSummaryDto } from "../types/groups";
import type { UserConnectionDto } from "../types/social";
import { formatPeso, signedPeso } from "../utils/format";

export default function Groups() {
  const navigate = useNavigate();
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

  const handleSearchUsers = async () => {
    const keyword = connectionQuery.trim();
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
          <h1 className="text-3xl font-bold text-gray-900">Groups</h1>
          <p className="text-sm text-gray-400 mt-1">Manage your shared expense groups</p>
        </div>
        <button
          onClick={() => setModalOpen(true)}
          className="flex items-center gap-2 px-5 py-2.5 text-sm font-bold text-white rounded-xl transition cursor-pointer"
          style={{ background: "#662498" }}
        >
          + New Group
        </button>
      </div>

      {error && <div className="rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{error}</div>}
      {connectionError && <div className="rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{connectionError}</div>}

      <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-5">
        <div>
          <h2 className="text-base font-bold text-gray-800 mb-3">Find users</h2>
          <div className="flex gap-3">
            <input
              value={connectionQuery}
              onChange={(event) => setConnectionQuery(event.target.value)}
              onKeyDown={(event) => event.key === "Enter" && handleSearchUsers()}
              placeholder="Search by name or email"
              className="flex-1 rounded-xl border border-gray-200 px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-purple-200"
            />
            <button
              onClick={handleSearchUsers}
              className="rounded-xl px-5 py-2.5 text-sm font-bold text-white transition cursor-pointer"
              style={{ background: "#662498" }}
            >
              Search
            </button>
          </div>

          <div className="mt-4 space-y-2">
            {connectionLoading ? (
              <p className="text-sm text-gray-500">Searching users...</p>
            ) : connectionResults.length === 0 ? (
              <></>
            ) : (
              connectionResults.map((user) => (
                <div key={user.id} className="rounded-xl border border-gray-100 px-4 py-3 flex items-center justify-between gap-3">
                  <div>
                    <p className="text-sm font-semibold text-gray-800">{user.firstname} {user.lastname}</p>
                    <p className="text-xs text-gray-500">{user.email}</p>
                    <p className="text-xs mt-1" style={{ color: user.mutual ? "#16a34a" : "#6b7280" }}>
                      {
                        user.mutual
                          ? "Mutual connection"
                          : user.following
                            ? "Waiting for this user to follow you back"
                            : user.followedBy
                              ? "This user follows you. Follow back to make it mutual"
                              : "Not followed yet"
                      }
                    </p>
                  </div>
                  <button
                    onClick={() => toggleFollow(user)}
                    className="rounded-lg px-4 py-2 text-xs font-bold transition cursor-pointer"
                    style={{
                      background: user.following ? "#e5e7eb" : "#662498",
                      color: user.following ? "#374151" : "#ffffff",
                    }}
                  >
                    {user.following ? "Unfollow" : "Follow"}
                  </button>
                </div>
              ))
            )}
          </div>
        </div>

        <div className="my-6 border-t border-gray-100" />

        <div className="w-full">
          <input
            value={groupSearch}
            onChange={(e) => setGroupSearch(e.target.value)}
            placeholder="Search groups"
            className="w-full px-3 py-2.5 rounded-xl border border-gray-200 focus:outline-none focus:ring-2 focus:ring-purple-200"
          />
        </div>

        {loading ? (
          <div className="mt-5 rounded-xl border border-dashed border-gray-300 px-4 py-8 text-center text-sm text-gray-500">Loading groups...</div>
        ) : (
          <div className="mt-5 space-y-3">
          {filteredGroups.length === 0 ? (
            <div className="rounded-xl border border-dashed border-gray-300 px-4 py-8 text-center text-sm text-gray-500">No groups found.</div>
          ) : filteredGroups.map((group) => (
            <button
              key={group.id}
              onClick={() => navigate(`/groups/${group.id}`)}
              className="w-full rounded-xl border border-gray-100 shadow-sm bg-white px-4 py-4 flex items-center justify-between gap-4 text-left cursor-pointer hover:border-purple-200 transition"
            >
              <div className="flex items-center gap-4">
                <div className="w-12 h-12 rounded-lg" style={{ background: "#d4d4d8" }} />
                <div>
                  <p className="text-xl font-bold text-gray-900">{group.name}</p>
                  <p className="text-xs text-gray-500 mt-0.5">{group.members.join(", ")}</p>
                  <p className="text-xs text-gray-400 mt-1">Total: {formatPeso(group.total)}</p>
                </div>
              </div>
              <div className="text-right">
                <p className="text-3xl font-bold" style={{ color: group.balance >= 0 ? "#16a34a" : "#dc2626" }}>{signedPeso(group.balance)}</p>
                <p className="text-xs text-gray-500 mt-0.5">{group.balance >= 0 ? "you are owed" : "you owe"}</p>
              </div>
            </button>
          ))}
          </div>
        )}
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
