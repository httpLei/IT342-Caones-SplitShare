import { Link, Navigate, useParams } from "react-router-dom";
import { GROUPS, formatPeso, signedPeso } from "../data/mockData";

export default function GroupDetails() {
  const { groupId } = useParams();
  const selectedGroup = GROUPS.find((group) => group.id === groupId);

  if (!selectedGroup) {
    return <Navigate to="/groups" replace />;
  }

  return (
    <div className="space-y-8">
      <div className="flex items-start justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">{selectedGroup.name}</h1>
          <p className="text-xs text-gray-500 mt-1">{selectedGroup.members.length} members, {formatPeso(selectedGroup.total)} total</p>
        </div>
        <div className="flex items-center gap-3">
          <Link
            to="/groups"
            className="px-4 py-2 text-sm font-bold rounded-xl border border-gray-200 text-gray-700 hover:bg-gray-50 transition"
          >
            Back to Groups
          </Link>
          <button className="flex items-center gap-2 px-5 py-2.5 text-sm font-bold text-white rounded-xl transition cursor-pointer" style={{ background: "#662498" }}>
            + Add Expense
          </button>
        </div>
      </div>

      <section className="bg-white rounded-2xl border border-gray-100 shadow-sm p-5">
        <h3 className="text-base font-bold text-gray-900 mb-3">Balances</h3>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
          {selectedGroup.balances.map((balance) => (
            <div key={balance.name} className="rounded-xl border border-gray-100 shadow-sm px-3 py-2 flex items-center justify-between gap-3">
              <div className="flex items-center gap-3">
                <div className="w-8 h-8 rounded-full flex items-center justify-center text-xs font-bold text-gray-600" style={{ background: "#e5e7eb" }}>
                  {balance.initial}
                </div>
                <div>
                  <p className="text-xs font-semibold text-gray-800">{balance.name}</p>
                  <p className="text-[10px] text-gray-500">{balance.positive ? "owes you" : "you owe"}</p>
                </div>
              </div>
              <div className="flex items-center gap-2">
                <p className="text-xs font-bold" style={{ color: balance.positive ? "#16a34a" : "#dc2626" }}>
                  {signedPeso(balance.amount)}
                </p>
                <button className="px-3 py-1 text-xs font-semibold rounded-lg cursor-pointer" style={{ background: "#dbeafe", color: "#1e3a8a" }}>
                  Settle
                </button>
              </div>
            </div>
          ))}
        </div>
      </section>

      <section className="bg-white rounded-2xl border border-gray-100 shadow-sm p-5">
        <h3 className="text-base font-bold text-gray-900 mb-3">Expenses</h3>
        <div className="space-y-2">
          {selectedGroup.expenses.map((expense, i) => (
            <div key={`${selectedGroup.id}-${i}`} className="rounded-xl border border-gray-100 shadow-sm px-4 py-3 flex items-center justify-between gap-4">
              <div>
                <p className="text-sm font-semibold text-gray-800">{expense.desc}</p>
                <p className="text-xs text-gray-400 mt-0.5">{expense.sub}</p>
              </div>
              <div className="text-right">
                <p className="text-sm font-semibold text-gray-700">P{expense.amount}</p>
                <p className="text-xs font-bold mt-0.5" style={{ color: expense.positive ? "#16a34a" : "#dc2626" }}>
                  {expense.share.startsWith("-") ? `-P${expense.share.replace("-", "")}` : `+P${expense.share.replace("+", "")}`}
                </p>
              </div>
            </div>
          ))}
        </div>
      </section>
    </div>
  );
}
