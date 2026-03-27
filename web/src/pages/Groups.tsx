import { useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { GROUPS, formatPeso, signedPeso } from "../data/mockData";

export default function Groups() {
  const navigate = useNavigate();
  const [groupSearch, setGroupSearch] = useState("");

  const filteredGroups = useMemo(() => {
    const keyword = groupSearch.trim().toLowerCase();
    if (!keyword) return GROUPS;
    return GROUPS.filter((group) => group.name.toLowerCase().includes(keyword) || group.members.join(" ").toLowerCase().includes(keyword));
  }, [groupSearch]);

  return (
    <div className="space-y-8">
      <div className="flex items-start justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Groups</h1>
          <p className="text-sm text-gray-400 mt-1">Manage your shared expense groups</p>
        </div>
        <button className="flex items-center gap-2 px-5 py-2.5 text-sm font-bold text-white rounded-xl transition cursor-pointer" style={{ background: "#662498" }}>
          + New Groups
        </button>
      </div>

      <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-5">
        <div className="max-w-xl">
          <input
            value={groupSearch}
            onChange={(e) => setGroupSearch(e.target.value)}
            placeholder="Search groups"
            className="w-full px-3 py-2.5 rounded-xl border border-gray-200 focus:outline-none focus:ring-2 focus:ring-purple-200"
          />
        </div>

        <div className="mt-5 space-y-3">
          {filteredGroups.map((group) => (
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
      </div>
    </div>
  );
}
