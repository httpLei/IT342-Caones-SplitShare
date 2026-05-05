import { useEffect, useState } from "react";
import { Link, Navigate, useNavigate, useParams } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";
import AddExpenseModal from "../components/AddExpenseModal";
import EditGroupModal from "../components/EditGroupModal";
import { groupApi } from "../services/groupService";
import { userApi } from "../../profile/services/userService";
import type { GroupDetailsDto } from "../types/groups";
import type { UserConnectionDto } from "../../profile/types/social";
import { formatPeso, signedPeso } from "../../../shared/utils/format";

export default function GroupDetailsPage() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const { groupId } = useParams();
  const id = Number(groupId);
  const [group, setGroup] = useState<GroupDetailsDto | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [showExpenseModal, setShowExpenseModal] = useState(false);
  const [saving, setSaving] = useState(false);
  const [saveError, setSaveError] = useState("");
  const [success, setSuccess] = useState("");
  const [showEditModal, setShowEditModal] = useState(false);
  const [editSaving, setEditSaving] = useState(false);
  const [editError, setEditError] = useState("");
  const [settlingEmail, setSettlingEmail] = useState<string | null>(null);
  const [mutuals, setMutuals] = useState<UserConnectionDto[]>([]);

  useEffect(() => {
    if (!Number.isFinite(id)) return;
    setError("");
    setLoading(true);
    groupApi.getGroup(id)
      .then((response) => setGroup(response.data.data ?? null))
      .catch((err) => setError(err?.response?.data?.error?.message ?? "Unable to load group."))
      .finally(() => setLoading(false));

    userApi.getMutuals()
      .then((response) => setMutuals(response.data.data ?? []))
      .catch(() => {
        // Non-blocking for edit modal; it will simply show no mutual users.
      });
  }, [id]);

  if (!Number.isFinite(id)) {
    return <Navigate to="/groups" replace />;
  }

  if (!loading && !group && !error) {
    return <Navigate to="/groups" replace />;
  }

  const handleAddExpense = async (payload: { description: string; category: string; amount: number; receipt?: File | null }) => {
    setSaving(true);
    setSaveError("");
    setSuccess("");
    try {
      const response = await groupApi.addExpense(id, payload);
      setGroup(response.data.data ?? null);
      setSuccess("Expense added successfully.");
      setShowExpenseModal(false);
    } catch (err: unknown) {
      setSaveError((err as { response?: { data?: { error?: { message?: string } } } })?.response?.data?.error?.message ?? "Unable to add expense.");
    } finally {
      setSaving(false);
    }
  };

  const handleUpdateGroup = async (payload: { name: string; memberEmails: string[] }) => {
    setEditSaving(true);
    setEditError("");
    try {
      const response = await groupApi.updateGroup(id, payload);
      setGroup(response.data.data ?? null);
      setSuccess("Group updated successfully.");
      setShowEditModal(false);
    } catch (err: unknown) {
      setEditError((err as { response?: { data?: { error?: { message?: string } } } })?.response?.data?.error?.message ?? "Unable to update group.");
    } finally {
      setEditSaving(false);
    }
  };

  if (loading) {
    return <div className="rounded-2xl border border-gray-100 bg-white p-6 text-sm text-gray-500">Loading group details...</div>;
  }

  if (error) {
    return <div className="rounded-2xl border border-red-200 bg-red-50 p-6 text-sm text-red-700">{error}</div>;
  }

  if (!group) {
    return <Navigate to="/groups" replace />;
  }

  const currentEmail = user?.email?.toLowerCase() ?? "";
  const displayBalances = group.balances.filter((b) => b.email.toLowerCase() !== currentEmail);

  const totalToReceive = displayBalances.reduce((sum, b) => sum + (b.positive ? b.amount : 0), 0);

  const handleSettle = async (counterpartEmail: string) => {
    setSuccess("");
    setSaveError("");
    setSettlingEmail(counterpartEmail);
    try {
      const response = await groupApi.settleBalance(id, counterpartEmail);
      setGroup(response.data.data ?? null);
      setSuccess("Settlement confirmation recorded. Balance clears after both members click Settle.");
    } catch (err: unknown) {
      setSaveError((err as { response?: { data?: { error?: { message?: string } } } })?.response?.data?.error?.message ?? "Unable to settle this balance.");
    } finally {
      setSettlingEmail(null);
    }
  };

  return (
    <div className="space-y-8">
      <div className="flex items-start justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">{group.name}</h1>
          <p className="text-xs text-gray-500 mt-1">{group.members.length} members, you will receive {formatPeso(totalToReceive)} total</p>
        </div>
        <div className="flex items-center gap-3">
          <Link
            to="/groups"
            className="px-4 py-2 text-sm font-bold rounded-xl border border-gray-200 text-gray-700 hover:bg-gray-50 transition"
          >
            Back to Groups
          </Link>
          <button
            onClick={() => setShowEditModal(true)}
            className="px-4 py-2 text-sm font-bold rounded-xl border border-purple-200 text-purple-700 hover:bg-purple-50 transition"
          >
            Edit Group
          </button>
          <button
            onClick={() => setShowExpenseModal(true)}
            className="flex items-center gap-2 px-5 py-2.5 text-sm font-bold text-white rounded-xl transition cursor-pointer"
            style={{ background: "#662498" }}
          >
            + Add Expense
          </button>
        </div>
      </div>

      {success && <div className="rounded-2xl border border-green-200 bg-green-50 px-4 py-3 text-sm text-green-700">{success}</div>}
      {saveError && <div className="rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{saveError}</div>}

      <section className="bg-white rounded-2xl border border-gray-100 shadow-sm p-5">
        <h3 className="text-base font-bold text-gray-900 mb-3">Balances</h3>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
          {displayBalances.map((balance) => (
            <div key={balance.name} className="rounded-xl border border-gray-100 shadow-sm px-3 py-2 flex items-center justify-between gap-3">
              <div className="flex items-center gap-3">
                <div className="w-8 h-8 rounded-full flex items-center justify-center text-xs font-bold text-gray-600" style={{ background: "#e5e7eb" }}>
                  {balance.initial}
                </div>
                <div>
                  <div className="flex items-center gap-2">
                    <p className="text-xs font-semibold text-gray-800">{balance.name}</p>
                    {balance.settlementPending && (
                      <span className="inline-flex items-center px-2 py-0.5 text-[10px] font-semibold rounded-full bg-amber-100 text-amber-800">
                        Pending
                      </span>
                    )}
                  </div>
                  <p className="text-[10px] text-gray-500">{balance.positive ? "owes you" : "you owe"}</p>
                  {balance.settlementPending && (
                    <p className="text-[10px] text-amber-600 mt-0.5">
                      {balance.settledByCurrentUser ? "Waiting for other user" : "Needs your confirmation"}
                    </p>
                  )}
                </div>
              </div>
              <div className="flex items-center gap-2">
                <p className="text-xs font-bold" style={{ color: balance.positive ? "#16a34a" : "#dc2626" }}>
                  {signedPeso(balance.amount)}
                </p>
                <button
                  onClick={() => handleSettle(balance.email)}
                  disabled={settlingEmail === balance.email || balance.amount <= 0 || (balance.settlementPending && balance.settledByCurrentUser)}
                  className="px-3 py-1 text-xs font-semibold rounded-lg cursor-pointer disabled:cursor-not-allowed disabled:opacity-60"
                  style={{ background: "#dbeafe", color: "#1e3a8a" }}
                >
                  {settlingEmail === balance.email
                    ? "Settling..."
                    : balance.settlementPending && balance.settledByCurrentUser
                      ? "Waiting..."
                      : balance.settlementPending
                        ? "Confirm Settle"
                        : "Settle"}
                </button>
              </div>
            </div>
          ))}
        </div>
      </section>

      <section className="bg-white rounded-2xl border border-gray-100 shadow-sm p-5">
        <h3 className="text-base font-bold text-gray-900 mb-3">Expenses</h3>
        <div className="space-y-2">
          {group.expenses.map((expense) => (
            <button
              key={expense.id}
              onClick={() => navigate(`/groups/${id}/expenses/${expense.id}`)}
              className="w-full rounded-xl border border-gray-100 shadow-sm px-4 py-3 flex items-center justify-between gap-4 text-left hover:border-purple-200 transition cursor-pointer"
            >
              <div>
                <p className="text-sm font-semibold text-gray-800">{expense.desc}</p>
                <p className="text-xs text-gray-400 mt-0.5">{expense.sub}</p>
              </div>
              <div className="text-right">
                <p className="text-sm font-semibold text-gray-700">{formatPeso(expense.amount)}</p>
                <p className="text-xs font-bold mt-0.5" style={{ color: expense.positive ? "#16a34a" : "#dc2626" }}>
                  {signedPeso(expense.share)}
                </p>
              </div>
            </button>
          ))}
        </div>
      </section>

      <AddExpenseModal
        open={showExpenseModal}
        loading={saving}
        error={saveError}
        onClose={() => setShowExpenseModal(false)}
        onSubmit={handleAddExpense}
      />

      <EditGroupModal
        open={showEditModal}
        loading={editSaving}
        error={editError}
        groupName={group.name}
        currentMembers={group.members.map((name, index) => ({
          name,
          email: group.memberEmails?.[index] ?? name,
        }))}
        mutuals={mutuals}
        onClose={() => setShowEditModal(false)}
        onSubmit={handleUpdateGroup}
      />
    </div>
  );
}
