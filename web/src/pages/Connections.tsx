import { useEffect, useState } from "react";
import axios from "axios";
import { userApi } from "../services/userService";
import type { UserConnectionDto } from "../types/social";

function getErrorMessage(err: unknown, fallback: string) {
  if (axios.isAxiosError(err)) {
    const apiMessage = err.response?.data?.error?.message;
    if (typeof apiMessage === "string" && apiMessage.trim()) {
      return apiMessage;
    }
    if (err.response?.status === 401) {
      return "Session expired. Please sign in again.";
    }
    if (!err.response) {
      return "Cannot reach backend API at http://localhost:8080.";
    }
    return `Request failed (${err.response.status}).`;
  }
  return fallback;
}

export default function Connections() {
  const [query, setQuery] = useState("");
  const [results, setResults] = useState<UserConnectionDto[]>([]);
  const [mutuals, setMutuals] = useState<UserConnectionDto[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const loadMutuals = async () => {
    const response = await userApi.getMutuals();
    setMutuals(response.data.data ?? []);
  };

  useEffect(() => {
    loadMutuals().catch(() => {
      setError("Unable to load mutual connections.");
    });
  }, []);

  const handleSearch = async () => {
    const keyword = query.trim();
    setError("");
    if (!keyword) {
      setResults([]);
      return;
    }

    setLoading(true);
    try {
      const response = await userApi.search(keyword);
      setResults(response.data.data ?? []);
    } catch (err: unknown) {
      setError(getErrorMessage(err, "Unable to search users."));
    } finally {
      setLoading(false);
    }
  };

  const toggleFollow = async (user: UserConnectionDto) => {
    try {
      if (user.following) {
        await userApi.unfollow(user.id);
      } else {
        await userApi.follow(user.id);
      }

      await Promise.all([handleSearch(), loadMutuals()]);
    } catch (err: unknown) {
      setError(getErrorMessage(err, "Unable to update follow status."));
    }
  };

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-3xl font-bold text-gray-900">Connections</h1>
        <p className="text-sm text-gray-400 mt-1">Search users and follow each other. Mutuals can be added to groups.</p>
      </div>

      {error && <div className="rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{error}</div>}

      <section className="rounded-2xl border border-gray-100 bg-white p-5 shadow-sm">
        <h2 className="text-base font-bold text-gray-800 mb-3">Find users</h2>
        <div className="flex gap-3">
          <input
            value={query}
            onChange={(event) => setQuery(event.target.value)}
            onKeyDown={(event) => event.key === "Enter" && handleSearch()}
            placeholder="Search by name or email"
            className="flex-1 rounded-xl border border-gray-200 px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-purple-200"
          />
          <button onClick={handleSearch} className="rounded-xl bg-purple-700 px-5 py-2.5 text-sm font-bold text-white hover:bg-purple-800 transition cursor-pointer">
            Search
          </button>
        </div>

        <div className="mt-4 space-y-2">
          {loading ? (
            <p className="text-sm text-gray-500">Searching users...</p>
          ) : results.length === 0 ? (
            <p className="text-sm text-gray-500">No users yet. Try another keyword.</p>
          ) : (
            results.map((user) => (
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
      </section>

      <section className="rounded-2xl border border-gray-100 bg-white p-5 shadow-sm">
        <h2 className="text-base font-bold text-gray-800 mb-3">Mutuals</h2>
        {mutuals.length === 0 ? (
          <p className="text-sm text-gray-500">No mutual connections yet. Following one-way is not enough. Both users must follow each other.</p>
        ) : (
          <div className="space-y-2">
            {mutuals.map((user) => (
              <div key={user.id} className="rounded-xl border border-green-100 bg-green-50 px-4 py-3">
                <p className="text-sm font-semibold text-gray-800">{user.firstname} {user.lastname}</p>
                <p className="text-xs text-gray-500">{user.email}</p>
              </div>
            ))}
          </div>
        )}
      </section>
    </div>
  );
}