import { useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { ACTIVITY } from "../data/mockData";

export default function Activity() {
  const navigate = useNavigate();
  const [activitySearch, setActivitySearch] = useState("");

  const activityRows = useMemo(
    () => [...ACTIVITY, ...ACTIVITY].map((item, index) => ({ ...item, rowKey: `${item.id}-${index}` })),
    []
  );

  const filteredActivity = useMemo(() => {
    const keyword = activitySearch.trim().toLowerCase();
    if (!keyword) return activityRows;
    return activityRows.filter((item) => item.desc.toLowerCase().includes(keyword) || item.sub.toLowerCase().includes(keyword) || item.amount.toLowerCase().includes(keyword));
  }, [activitySearch, activityRows]);

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-3xl font-bold text-gray-900">Activity</h1>
        <p className="text-sm text-gray-400 mt-1">Your complete transaction history</p>
      </div>

      <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-5">
        <div className="max-w-xl">
          <input
            value={activitySearch}
            onChange={(e) => setActivitySearch(e.target.value)}
            placeholder="Search transactions"
            className="w-full px-3 py-2.5 rounded-xl border border-gray-200 focus:outline-none focus:ring-2 focus:ring-purple-200"
          />
        </div>

        <div className="mt-5 space-y-3">
          {filteredActivity.length === 0 ? (
            <div className="rounded-xl border border-dashed border-gray-300 px-4 py-8 text-center text-sm text-gray-500">No activity found for that search.</div>
          ) : (
            filteredActivity.map((a) => (
              <button
                key={a.rowKey}
                onClick={() => navigate(`/activity/${a.id}`)}
                className="w-full rounded-xl border border-gray-100 shadow-sm bg-white px-4 py-3 flex items-center justify-between gap-4 text-left cursor-pointer hover:border-purple-200 transition"
              >
                <div>
                  <p className="text-sm font-semibold text-gray-800">{a.desc}</p>
                  <p className="text-xs text-gray-400 mt-0.5">{a.sub}</p>
                </div>
                <div className="text-right">
                  <p className="text-sm font-semibold text-gray-700">{a.amount}</p>
                  <p className="text-xs font-bold mt-0.5" style={{ color: a.positive ? "#16a34a" : "#dc2626" }}>{a.share}</p>
                </div>
              </button>
            ))
          )}
        </div>
      </div>
    </div>
  );
}
