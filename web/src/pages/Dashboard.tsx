import { useAuth } from "../context/AuthContext";
import { ACTIVITY, GROUPS, formatPeso, signedPeso } from "../data/mockData";

export default function Dashboard() {
  const { user } = useAuth();

  return (
    <div className="space-y-8">
      <div className="flex items-start justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Hello, {user?.firstname}!</h1>
          <p className="text-sm text-gray-400 mt-1">Here&apos;s your expense summary</p>
        </div>
        <button className="flex items-center gap-2 px-5 py-2.5 text-sm font-bold text-white rounded-xl transition cursor-pointer" style={{ background: "#662498" }}>
          + Add Expense
        </button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
        {[
          { label: "Net Balance", value: "+1,484.42", sub: "You are owed overall", color: "#662498", bg: "#f5f0ff" },
          { label: "You are owed", value: "+1,724.42", sub: "You are owed overall", color: "#16a34a", bg: "#f0fdf4" },
          { label: "You owe", value: "240.00", sub: "You owe overall", color: "#dc2626", bg: "#fef2f2" }
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
        <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
          {GROUPS.slice(0, 2).map((group) => (
            <div key={group.id} className="rounded-2xl p-5 border border-gray-100 shadow-sm bg-white">
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
            </div>
          ))}
        </div>
      </div>

      <div>
        <h2 className="text-base font-bold text-gray-800 mb-4">Recent Activity</h2>
        <div className="bg-white rounded-2xl border border-gray-100 shadow-sm divide-y divide-gray-50">
          {ACTIVITY.map((item, i) => (
            <div key={`${item.desc}-${i}`} className="flex items-center justify-between px-6 py-4">
              <div>
                <p className="text-sm font-medium text-gray-800">{item.desc}</p>
                <p className="text-xs text-gray-400 mt-0.5">{item.sub}</p>
              </div>
              <div className="text-right">
                <p className="text-sm font-semibold text-gray-700">{item.amount}</p>
                <p className="text-xs font-bold mt-0.5" style={{ color: item.positive ? "#16a34a" : "#dc2626" }}>{item.share}</p>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
