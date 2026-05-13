import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Search } from "lucide-react";
import { useCurrency } from "../../settings/contexts/CurrencyContext";
import { userApi } from "../../profile/services/userService";
import type { UserActivityDto } from "../../profile/types/social";
import { formatCurrency, signedCurrency } from "../../../shared/utils/format";

export default function ActivityPage() {
  const navigate = useNavigate();
  const { currency } = useCurrency();
  const [activitySearch, setActivitySearch] = useState("");
  const [activityRows, setActivityRows] = useState<UserActivityDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    setLoading(true);
    setError("");
    userApi.getMyHistory()
      .then((response) => setActivityRows(response.data.data ?? []))
      .catch((err: unknown) => {
        const message = (err as { response?: { data?: { error?: { message?: string } } } })?.response?.data?.error?.message;
        setError(message ?? "Unable to load activity history.");
      })
      .finally(() => setLoading(false));
  }, []);

  const filteredActivity = useMemo(() => {
    const keyword = activitySearch.trim().toLowerCase();
    if (!keyword) return activityRows;
    return activityRows.filter((item) =>
      item.desc.toLowerCase().includes(keyword)
      || item.sub.toLowerCase().includes(keyword)
      || item.amount.toString().includes(keyword)
      || item.share.toString().includes(keyword)
    );
  }, [activitySearch, activityRows]);

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-4xl font-bold text-gray-900 dark:text-white">Activity 📊</h1>
        <p className="text-sm text-gray-500 dark:text-gray-400 mt-2">Your complete transaction history</p>
      </div>

      <div className="bg-white dark:bg-gray-800 rounded-2xl border border-gray-100 dark:border-gray-700 shadow-sm p-6 transition duration-300">
        <div className="max-w-xl mb-5">
          <div className="relative">
            <Search size={18} className="absolute left-4 top-3.5 text-gray-400" />
            <input
              value={activitySearch}
              onChange={(e) => setActivitySearch(e.target.value)}
              placeholder="Search transactions"
              className="w-full pl-12 pr-4 py-2.5 rounded-xl border border-gray-200 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-purple-500 transition"
            />
          </div>
        </div>

        {error && <div className="rounded-xl border border-red-200 dark:border-red-800 bg-red-50 dark:bg-red-900/30 px-4 py-3 text-sm text-red-700 dark:text-red-300">{error}</div>}

        <div className="space-y-3">
          {loading ? (
            <div className="rounded-xl border border-dashed border-gray-300 dark:border-gray-600 px-4 py-8 text-center text-sm text-gray-500 dark:text-gray-400">
              <div className="inline-block animate-spin rounded-full h-6 w-6 border-b-2 border-purple-600 mb-2"></div>
              <p>Loading activity...</p>
            </div>
          ) : filteredActivity.length === 0 ? (
            <div className="rounded-xl border border-dashed border-gray-300 dark:border-gray-600 px-4 py-8 text-center text-sm text-gray-500 dark:text-gray-400">No activity found for that search.</div>
          ) : (
            filteredActivity.map((a) => (
              <button
                key={a.id}
                onClick={() => navigate(`/activity/${a.id}`)}
                className="w-full rounded-xl border border-gray-100 dark:border-gray-700 shadow-sm bg-white dark:bg-gray-700 px-4 py-3 flex items-center justify-between gap-4 text-left cursor-pointer hover:border-purple-200 dark:hover:border-purple-500 transition"
              >
                <div>
                  <p className="text-sm font-semibold text-gray-800 dark:text-white">{a.desc}</p>
                  <p className="text-xs text-gray-500 dark:text-gray-400 mt-0.5">{a.sub}</p>
                </div>
                <div className="text-right">
                  <p className="text-sm font-semibold text-gray-700 dark:text-gray-300">{formatCurrency(a.amount, currency)}</p>
                  <p className="text-xs font-bold mt-0.5" style={{ color: a.positive ? "#16a34a" : "#dc2626" }}>{signedCurrency(a.share, currency)}</p>
                </div>
              </button>
            ))
          )}
        </div>
      </div>
    </div>
  );
}
