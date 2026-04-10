import { useEffect, useState } from "react";
import { Link, Navigate, useNavigate, useParams } from "react-router-dom";
import AddExpenseModal from "../components/AddExpenseModal";
import { expenseApi } from "../services/groupService";
import type { ExpenseDto } from "../types/groups";
import { formatPeso, signedPeso } from "../utils/format";

export default function ActivityDetails() {
  const navigate = useNavigate();
  const { activityId, groupId } = useParams();
  const id = Number(activityId);
  const gid = Number(groupId);

  const [expense, setExpense] = useState<ExpenseDto | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [showEditModal, setShowEditModal] = useState(false);
  const [showReceiptPreview, setShowReceiptPreview] = useState(false);
  const [receiptLoading, setReceiptLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [saveError, setSaveError] = useState("");
  const [success, setSuccess] = useState("");

  useEffect(() => {
    if (!Number.isFinite(id)) return;

    setLoading(true);
    setError("");
    expenseApi.getExpense(id)
      .then((response) => setExpense(response.data.data ?? null))
      .catch((err: unknown) => {
        const message = (err as { response?: { data?: { error?: { message?: string } } } })?.response?.data?.error?.message;
        setError(message ?? "Unable to load expense details.");
      })
      .finally(() => setLoading(false));
  }, [id]);

  if (!Number.isFinite(id)) {
    return <Navigate to="/activity" replace />;
  }

  if (loading) {
    return <div className="rounded-2xl border border-gray-100 bg-white p-6 text-sm text-gray-500">Loading activity details...</div>;
  }

  if (error) {
    return <div className="rounded-2xl border border-red-200 bg-red-50 p-6 text-sm text-red-700">{error}</div>;
  }

  if (!expense) {
    return <Navigate to={Number.isFinite(gid) ? `/groups/${gid}` : "/activity"} replace />;
  }

  const detail = expense;

  const handleUpdateExpense = async (payload: { description: string; category: string; amount: number; receipt?: File | null }) => {
    setSaving(true);
    setSaveError("");
    setSuccess("");
    try {
      const response = await expenseApi.updateExpense(id, payload);
      setExpense(response.data.data ?? null);
      setSuccess("Expense updated successfully.");
      setShowEditModal(false);
    } catch (err: unknown) {
      setSaveError((err as { response?: { data?: { error?: { message?: string } } } })?.response?.data?.error?.message ?? "Unable to update expense.");
    } finally {
      setSaving(false);
    }
  };

  const handleDeleteExpense = async () => {
    if (!window.confirm("Delete this expense?")) return;

    setSaving(true);
    setSaveError("");
    setSuccess("");
    try {
      await expenseApi.deleteExpense(id);
      if (Number.isFinite(gid)) {
        navigate(`/groups/${gid}`, { replace: true });
      } else {
        navigate("/activity", { replace: true });
      }
    } catch (err: unknown) {
      setSaveError((err as { response?: { data?: { error?: { message?: string } } } })?.response?.data?.error?.message ?? "Unable to delete expense.");
    } finally {
      setSaving(false);
    }
  };

  const isCredit = detail.positive;

  return (
    <div className="space-y-8">
      <div className="flex items-start justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Activity Details</h1>
          <p className="text-sm text-gray-400 mt-1">View complete transaction information</p>
        </div>
        <Link
          to={Number.isFinite(gid) ? `/groups/${gid}` : "/activity"}
          className="px-4 py-2 text-sm font-bold rounded-xl border border-gray-200 text-gray-700 hover:bg-gray-50 transition"
        >
          Back
        </Link>
      </div>

      {success && <div className="rounded-2xl border border-green-200 bg-green-50 p-4 text-sm text-green-700">{success}</div>}
      {saveError && <div className="rounded-2xl border border-red-200 bg-red-50 p-4 text-sm text-red-700">{saveError}</div>}

      <section className="bg-white rounded-2xl border border-gray-100 shadow-sm p-6 space-y-5">
        <div>
          <p className="text-xs uppercase tracking-wider text-gray-500 font-semibold">Description</p>
          <p className="text-lg font-bold text-gray-900 mt-1">{detail.desc}</p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <p className="text-xs uppercase tracking-wider text-gray-500 font-semibold">Group and Date</p>
            <p className="text-sm font-medium text-gray-800 mt-1">{detail.sub}</p>
          </div>
          <div>
            <p className="text-xs uppercase tracking-wider text-gray-500 font-semibold">Total Amount</p>
            <p className="text-sm font-medium text-gray-800 mt-1">{formatPeso(detail.amount)}</p>
          </div>
          <div>
            <p className="text-xs uppercase tracking-wider text-gray-500 font-semibold">Your Share</p>
            <p className="text-sm font-bold mt-1" style={{ color: isCredit ? "#16a34a" : "#dc2626" }}>
              {signedPeso(detail.share)}
            </p>
          </div>
          <div>
            <p className="text-xs uppercase tracking-wider text-gray-500 font-semibold">Status</p>
            <p className="text-sm font-medium mt-1" style={{ color: isCredit ? "#16a34a" : "#dc2626" }}>
              {isCredit ? "Credit" : "Debit"}
            </p>
          </div>
          <div>
            <p className="text-xs uppercase tracking-wider text-gray-500 font-semibold">Category</p>
            <p className="text-sm font-medium text-gray-800 mt-1">{detail.category || "N/A"}</p>
          </div>
          <div>
            <p className="text-xs uppercase tracking-wider text-gray-500 font-semibold">Receipt</p>
            {detail.receiptUrl ? (
              <button
                type="button"
                onClick={() => {
                  setReceiptLoading(true);
                  setShowReceiptPreview(true);
                }}
                className="text-sm font-semibold text-purple-700 hover:underline mt-1 inline-block"
              >
                View receipt image
              </button>
            ) : (
              <p className="text-sm text-gray-500 mt-1">No receipt attached.</p>
            )}
          </div>
        </div>

        <div className="flex gap-3 pt-2">
          <button
            onClick={() => setShowEditModal(true)}
            className="px-4 py-2 text-sm font-bold rounded-xl border border-purple-200 text-purple-700 hover:bg-purple-50 transition"
          >
            Edit Expense
          </button>
          <button
            onClick={handleDeleteExpense}
            className="px-4 py-2 text-sm font-bold rounded-xl border border-red-200 text-red-700 hover:bg-red-50 transition"
            disabled={saving}
          >
            Delete Expense
          </button>
        </div>
      </section>

      <AddExpenseModal
        open={showEditModal}
        loading={saving}
        error={saveError}
        mode="edit"
        initialValues={{
          description: detail.description,
          category: detail.category,
          amount: Number(detail.amount),
          receiptUrl: detail.receiptUrl,
        }}
        onClose={() => setShowEditModal(false)}
        onSubmit={handleUpdateExpense}
      />

      {showReceiptPreview && detail.receiptUrl && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 p-4"
          onClick={() => {
            setShowReceiptPreview(false);
            setReceiptLoading(false);
          }}
        >
          <div
            className="relative w-full max-w-3xl rounded-2xl bg-white p-3 shadow-2xl"
            onClick={(event) => event.stopPropagation()}
          >
            <button
              type="button"
              onClick={() => {
                setShowReceiptPreview(false);
                setReceiptLoading(false);
              }}
              className="absolute right-3 top-3 rounded-lg border border-gray-200 bg-white px-3 py-1 text-xs font-semibold text-gray-700 hover:bg-gray-50"
            >
              Close
            </button>
            {receiptLoading && (
              <div className="absolute inset-0 flex items-center justify-center rounded-xl bg-white/80">
                <div className="h-10 w-10 animate-spin rounded-full border-4 border-purple-200 border-t-purple-700" />
              </div>
            )}
            <img
              src={detail.receiptUrl}
              alt="Expense receipt"
              onLoad={() => setReceiptLoading(false)}
              onError={() => setReceiptLoading(false)}
              className={`max-h-[80vh] w-full rounded-xl object-contain transition-opacity ${receiptLoading ? "opacity-0" : "opacity-100"}`}
            />
          </div>
        </div>
      )}
    </div>
  );
}
