import { useEffect, useMemo, useState } from "react";
import { Link, Navigate, useParams } from "react-router-dom";
import { userApi } from "../services/userService";
import type { UserActivityDto } from "../types/social";
import { formatPeso, signedPeso } from "../utils/format";

export default function ActivityDetails() {
  const { activityId } = useParams();
  const id = Number(activityId);
  const [rows, setRows] = useState<UserActivityDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!Number.isFinite(id)) return;
    setLoading(true);
    setError("");
    userApi.getMyHistory()
      .then((response) => setRows(response.data.data ?? []))
      .catch((err: unknown) => {
        const message = (err as { response?: { data?: { error?: { message?: string } } } })?.response?.data?.error?.message;
        setError(message ?? "Unable to load activity details.");
      })
      .finally(() => setLoading(false));
  }, [id]);

  const activity = useMemo(() => rows.find((item) => item.id === id), [rows, id]);

  if (!Number.isFinite(id)) {
    return <Navigate to="/activity" replace />;
  }

  if (loading) {
    return <div className="rounded-2xl border border-gray-100 bg-white p-6 text-sm text-gray-500">Loading activity details...</div>;
  }

  if (error) {
    return <div className="rounded-2xl border border-red-200 bg-red-50 p-6 text-sm text-red-700">{error}</div>;
  }

  if (!activity) {
    return <Navigate to="/activity" replace />;
  }

  const isCredit = activity.positive;

  return (
    <div className="space-y-8">
      <div className="flex items-start justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Activity Details</h1>
          <p className="text-sm text-gray-400 mt-1">View complete transaction information</p>
        </div>
        <Link
          to="/activity"
          className="px-4 py-2 text-sm font-bold rounded-xl border border-gray-200 text-gray-700 hover:bg-gray-50 transition"
        >
          Back to Activity
        </Link>
      </div>

      <section className="bg-white rounded-2xl border border-gray-100 shadow-sm p-6 space-y-5">
        <div>
          <p className="text-xs uppercase tracking-wider text-gray-500 font-semibold">Description</p>
          <p className="text-lg font-bold text-gray-900 mt-1">{activity.desc}</p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <p className="text-xs uppercase tracking-wider text-gray-500 font-semibold">Group and Date</p>
            <p className="text-sm font-medium text-gray-800 mt-1">{activity.sub}</p>
          </div>
          <div>
            <p className="text-xs uppercase tracking-wider text-gray-500 font-semibold">Total Amount</p>
            <p className="text-sm font-medium text-gray-800 mt-1">{formatPeso(activity.amount)}</p>
          </div>
          <div>
            <p className="text-xs uppercase tracking-wider text-gray-500 font-semibold">Your Share</p>
            <p className="text-sm font-bold mt-1" style={{ color: isCredit ? "#16a34a" : "#dc2626" }}>
              {signedPeso(activity.share)}
            </p>
          </div>
          <div>
            <p className="text-xs uppercase tracking-wider text-gray-500 font-semibold">Status</p>
            <p className="text-sm font-medium mt-1" style={{ color: isCredit ? "#16a34a" : "#dc2626" }}>
              {isCredit ? "Credit" : "Debit"}
            </p>
          </div>
        </div>
      </section>
    </div>
  );
}
