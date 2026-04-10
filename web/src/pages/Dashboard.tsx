import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { groupApi } from "../services/groupService";
import type { GroupSummaryDto } from "../types/groups";
import { formatPeso, signedPeso } from "../utils/format";

export default function Dashboard() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [groups, setGroups] = useState<GroupSummaryDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let active = true;
    setLoading(true);
    groupApi.getGroups()
      .then((response) => {
        if (!active) return;
        setGroups(response.data.data ?? []);
      })
      .catch((err) => {
        if (!active) return;
        setError(err?.response?.data?.error?.message ?? "Unable to load groups.");
      })
      .finally(() => {
        if (active) setLoading(false);
      });

    return () => {
      active = false;
    };
  }, []);

  const metrics = useMemo(() => {
    const positive = groups.reduce((sum, group) => sum + Math.max(group.balance, 0), 0);
    const negative = groups.reduce((sum, group) => sum + Math.abs(Math.min(group.balance, 0)), 0);
    return {
      net: positive - negative,
      owed: positive,
      owe: negative,
    };
  }, [groups]);

  return (
    <div className="space-y-8">
      <div className="flex items-start justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Hello, {user?.firstname}!</h1>
          <p className="text-sm text-gray-400 mt-1">Here&apos;s your shared expense summary</p>
        </div>
        <button
          onClick={() => navigate("/groups")}
          className="flex items-center gap-2 px-5 py-2.5 text-sm font-bold text-white rounded-xl transition cursor-pointer"
          style={{ background: "#662498" }}
        >
          + Add Expense
        </button>
      </div>

      {error && <div className="rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{error}</div>}

      <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
        {[
          { label: "Net Balance", value: signedPeso(metrics.net), sub: "Across all groups", color: "#662498", bg: "#f5f0ff" },
          { label: "You are owed", value: signedPeso(metrics.owed), sub: "Positive balances", color: "#16a34a", bg: "#f0fdf4" },
          { label: "You owe", value: signedPeso(-metrics.owe), sub: "Negative balances", color: "#dc2626", bg: "#fef2f2" }
        ].map((card) => (
          <div key={card.label} className="rounded-2xl p-5 border border-gray-100 shadow-sm" style={{ background: card.bg }}>
            <p className="text-xs font-semibold text-gray-400 uppercase tracking-wide mb-3">{card.label}</p>
            <p className="text-2xl font-bold" style={{ color: card.color }}>{card.value}</p>
            <p className="text-xs text-gray-400 mt-1">{card.sub}</p>
          </div>
        ))}
      </div>

      <div>
        <h2 className="text-base font-bold text-gray-800 mb-4">Your Groups</h2>
        {loading ? (
          <div className="rounded-2xl border border-gray-100 bg-white p-6 text-sm text-gray-500">Loading groups...</div>
        ) : groups.length === 0 ? (
          <div className="rounded-2xl border border-dashed border-gray-300 bg-white p-8 text-center text-sm text-gray-500">
            No groups yet. Create one in the Groups page to start logging shared expenses.
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
            {groups.slice(0, 2).map((group) => (
              <button
                key={group.id}
                onClick={() => navigate(`/groups/${group.id}`)}
                className="rounded-2xl p-5 border border-gray-100 shadow-sm bg-white text-left hover:border-purple-200 transition cursor-pointer"
              >
                <p className="font-bold text-gray-900">{group.name}</p>
                <p className="text-xs text-gray-600 mt-0.5">{group.members.length} members</p>
                <div className="flex items-end justify-between mt-6">
                  <p className="text-xs text-gray-600">{formatPeso(group.total)} total</p>
                  <p className="text-sm font-bold" style={{ color: group.balance >= 0 ? "#16a34a" : "#dc2626" }}>
                    {signedPeso(group.balance)}
                    <br />
                    <span className="text-xs font-normal text-gray-600">{group.balance >= 0 ? "you are owed" : "you owe"}</span>
                  </p>
                </div>
              </button>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
