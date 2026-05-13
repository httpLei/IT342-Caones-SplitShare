import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Plus, TrendingUp, TrendingDown, DollarSign, Users } from "lucide-react";
import { useAuth } from "../../auth/AuthContext";
import { useTheme } from "../../settings/contexts/ThemeContext";
import { useCurrency } from "../../settings/contexts/CurrencyContext";
import { groupApi } from "../../groups/services/groupService";
import type { GroupSummaryDto } from "../../groups/types/groups";
import { formatCurrency, signedCurrency } from "../../../shared/utils/format";

export default function DashboardPage() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const { isDark } = useTheme();
  const { currency } = useCurrency();
  const [groups, setGroups] = useState<GroupSummaryDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let active = true;
    setLoading(true);
    groupApi
      .getGroups()
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
    const positive = groups.reduce((sum, group) => sum + (group.owed ?? Math.max(group.balance, 0)), 0);
    const negative = groups.reduce((sum, group) => sum + (group.owe ?? Math.abs(Math.min(group.balance, 0))), 0);
    return {
      net: positive - negative,
      owed: positive,
      owe: negative,
    };
  }, [groups]);

  const cards = [
    { label: "Net Balance", value: signedCurrency(metrics.net, currency), sub: "Across all groups", color: "#662498", bg: "#f5f0ff", darkBg: "#3d2463", icon: DollarSign },
    { label: "You are owed", value: signedCurrency(metrics.owed, currency), sub: "Positive balances", color: "#16a34a", bg: "#f0fdf4", darkBg: "#1f3a1f", icon: TrendingUp },
    { label: "You owe", value: signedCurrency(-metrics.owe, currency), sub: "Negative balances", color: "#dc2626", bg: "#fef2f2", darkBg: "#3a1f1f", icon: TrendingDown },
  ];

  return (
    <div className="space-y-8">
      <div className="flex items-start justify-between">
        <div>
          <h1 className="text-4xl font-bold text-gray-900 dark:text-white">Hello, {user?.firstname}! 👋</h1>
          <p className="text-sm text-gray-500 dark:text-gray-400 mt-2">Here&apos;s your shared expense summary</p>
        </div>
        <button
          onClick={() => navigate("/groups")}
          className="flex items-center gap-2 px-6 py-3 text-sm font-bold text-white rounded-xl transition duration-200 hover:shadow-lg transform hover:scale-105"
          style={{ background: "linear-gradient(135deg, #662498 0%, #a855f7 100%)" }}
        >
          <Plus size={20} />
          Add Expense
        </button>
      </div>

      {error && <div className="rounded-2xl border border-red-200 bg-red-50 dark:bg-red-900/30 dark:border-red-800 px-4 py-3 text-sm text-red-700 dark:text-red-300">{error}</div>}

      <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
        {cards.map((card) => {
          const Icon = card.icon;
          return (
            <div key={card.label} className="rounded-2xl p-6 border border-gray-100 dark:border-gray-700 shadow-sm hover:shadow-md transition duration-200" style={{ background: isDark ? card.darkBg : card.bg }}>
              <div className="flex items-start justify-between mb-4">
                <p className="text-xs font-semibold text-gray-500 dark:text-gray-400 uppercase tracking-wider">{card.label}</p>
                <div className="p-2 rounded-lg" style={{ background: card.color + (isDark ? "33" : "20") }}>
                  <Icon size={20} style={{ color: card.color }} />
                </div>
              </div>
              <p className="text-3xl font-bold" style={{ color: card.color }}>{card.value}</p>
              <p className="text-xs text-gray-500 dark:text-gray-400 mt-2">{card.sub}</p>
            </div>
          );
        })}
      </div>

      <div>
        <div className="flex items-center justify-between mb-5">
          <h2 className="text-xl font-bold text-gray-900 dark:text-white">Your Groups</h2>
          {groups.length > 2 && <p className="text-xs text-gray-500 dark:text-gray-400">{groups.length} groups total</p>}
        </div>
        {loading ? (
          <div className="rounded-2xl border border-gray-100 dark:border-gray-700 bg-white dark:bg-gray-800 p-8 text-center">
            <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-purple-600"></div>
            <p className="text-sm text-gray-500 dark:text-gray-400 mt-3">Loading groups...</p>
          </div>
        ) : groups.length === 0 ? (
          <div className="rounded-2xl border-2 border-dashed border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-800 p-12 text-center">
            <div className="text-gray-400 mb-3 flex justify-center">📦</div>
            <p className="text-sm font-semibold text-gray-700 dark:text-gray-300">No groups yet</p>
            <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">Create one in the Groups page to start logging shared expenses</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
            {groups.slice(0, 2).map((group) => (
              <button
                key={group.id}
                onClick={() => navigate(`/groups/${group.id}`)}
                className="rounded-2xl p-6 border border-gray-100 dark:border-gray-700 shadow-sm bg-white dark:bg-gray-800 text-left hover:border-purple-300 dark:hover:border-purple-500 hover:shadow-md transition duration-200 transform hover:translate-y-[-2px]"
              >
                <div className="flex items-start justify-between mb-3">
                  <p className="font-bold text-gray-900 dark:text-white text-lg">{group.name}</p>
                  <div className="bg-purple-100 dark:bg-purple-900/30 rounded-lg p-2">
                    <Users size={18} style={{ color: "#662498" }} />
                  </div>
                </div>
                <p className="text-xs text-gray-500 dark:text-gray-400">{group.members.length} member{group.members.length !== 1 ? "s" : ""}</p>
                <div className="flex items-end justify-between mt-6 pt-4 border-t border-gray-100 dark:border-gray-700">
                  <p className="text-xs text-gray-500 dark:text-gray-400">{formatCurrency(group.total, currency)} total</p>
                  <div className="text-right">
                    <p className="text-sm font-bold" style={{ color: group.balance >= 0 ? "#16a34a" : "#dc2626" }}>
                      {signedCurrency(group.balance, currency)}
                    </p>
                    <p className="text-xs font-medium" style={{ color: group.balance >= 0 ? "#16a34a" : "#dc2626" }}>
                      {(group.owed ?? Math.max(group.balance, 0)) > 0 ? "owed to you" : "you owe"}
                    </p>
                  </div>
                </div>
              </button>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
