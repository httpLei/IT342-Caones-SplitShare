import { useEffect, useState, type FormEvent } from "react";

type Props = {
  open: boolean;
  loading?: boolean;
  error?: string;
  onClose: () => void;
  onSubmit: (payload: { description: string; category: string; amount: number }) => void;
};

export default function AddExpenseModal({ open, loading = false, error = "", onClose, onSubmit }: Props) {
  const [description, setDescription] = useState("");
  const [category, setCategory] = useState("Food");
  const [amount, setAmount] = useState("");

  useEffect(() => {
    if (!open) {
      setDescription("");
      setCategory("Food");
      setAmount("");
    }
  }, [open]);

  if (!open) return null;

  const handleSubmit = (event: FormEvent) => {
    event.preventDefault();
    onSubmit({ description, category, amount: Number(amount) });
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 px-4" onClick={onClose}>
      <div className="w-full max-w-lg rounded-2xl bg-white p-6 shadow-2xl" onClick={(event) => event.stopPropagation()}>
        <div className="flex items-start justify-between gap-4 mb-5">
          <div>
            <h3 className="text-xl font-bold text-gray-900">Add expense</h3>
            <p className="text-sm text-gray-500 mt-1">Log a shared cost for this group.</p>
          </div>
          <button onClick={onClose} className="text-sm font-semibold text-gray-400 hover:text-gray-700 transition cursor-pointer">
            Close
          </button>
        </div>

        {error && <div className="mb-4 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{error}</div>}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="mb-1.5 block text-xs font-semibold uppercase tracking-wide text-gray-500">Description</label>
            <input
              required
              value={description}
              onChange={(event) => setDescription(event.target.value)}
              className="w-full rounded-xl border border-gray-200 px-4 py-3 text-sm outline-none focus:border-purple-400"
              placeholder="Dinner, groceries, transport..."
            />
          </div>

          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <div>
              <label className="mb-1.5 block text-xs font-semibold uppercase tracking-wide text-gray-500">Category</label>
              <input
                required
                value={category}
                onChange={(event) => setCategory(event.target.value)}
                className="w-full rounded-xl border border-gray-200 px-4 py-3 text-sm outline-none focus:border-purple-400"
                placeholder="Food"
              />
            </div>
            <div>
              <label className="mb-1.5 block text-xs font-semibold uppercase tracking-wide text-gray-500">Amount</label>
              <input
                required
                type="number"
                min="0.01"
                step="0.01"
                value={amount}
                onChange={(event) => setAmount(event.target.value)}
                className="w-full rounded-xl border border-gray-200 px-4 py-3 text-sm outline-none focus:border-purple-400"
                placeholder="120.00"
              />
            </div>
          </div>

          <div className="flex gap-3 pt-1">
            <button type="button" onClick={onClose} className="flex-1 rounded-xl border border-gray-200 px-4 py-3 text-sm font-semibold text-gray-600 hover:bg-gray-50 transition cursor-pointer">
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading}
              className="flex-1 rounded-xl bg-purple-700 px-4 py-3 text-sm font-bold text-white transition hover:bg-purple-800 disabled:cursor-not-allowed disabled:opacity-60 cursor-pointer"
            >
              {loading ? "Saving..." : "Save expense"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}