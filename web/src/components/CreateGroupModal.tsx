import { useEffect, useState, type FormEvent } from "react";
import type { UserConnectionDto } from "../types/social";

type Props = {
  open: boolean;
  loading?: boolean;
  error?: string;
  mutuals: UserConnectionDto[];
  onClose: () => void;
  onSubmit: (payload: { name: string; memberEmails: string[] }) => void;
};

export default function CreateGroupModal({ open, loading = false, error = "", mutuals, onClose, onSubmit }: Props) {
  const [name, setName] = useState("");
  const [selectedEmails, setSelectedEmails] = useState<string[]>([]);

  useEffect(() => {
    if (!open) {
      setName("");
      setSelectedEmails([]);
    }
  }, [open]);

  if (!open) return null;

  const handleSubmit = (event: FormEvent) => {
    event.preventDefault();
    onSubmit({ name, memberEmails: selectedEmails });
  };

  const toggleSelection = (email: string) => {
    setSelectedEmails((prev) =>
      prev.includes(email) ? prev.filter((item) => item !== email) : [...prev, email]
    );
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 px-4" onClick={onClose}>
      <div className="w-full max-w-lg rounded-2xl bg-white p-6 shadow-2xl" onClick={(event) => event.stopPropagation()}>
        <div className="flex items-start justify-between gap-4 mb-5">
          <div>
            <h3 className="text-xl font-bold text-gray-900">Create group</h3>
            <p className="text-sm text-gray-500 mt-1">Start a shared expense group.</p>
          </div>
          <button onClick={onClose} className="text-sm font-semibold text-gray-400 hover:text-gray-700 transition cursor-pointer">
            Close
          </button>
        </div>

        {error && <div className="mb-4 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{error}</div>}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="mb-1.5 block text-xs font-semibold uppercase tracking-wide text-gray-500">Group name</label>
            <input
              required
              value={name}
              onChange={(event) => setName(event.target.value)}
              className="w-full rounded-xl border border-gray-200 px-4 py-3 text-sm outline-none focus:border-purple-400"
              placeholder="Roommates, Japan Trip, etc."
            />
          </div>

          <div>
            <label className="mb-1.5 block text-xs font-semibold uppercase tracking-wide text-gray-500">Mutual users to include</label>
            <div className="max-h-48 overflow-y-auto rounded-xl border border-gray-200 p-3">
              {mutuals.length === 0 ? (
                <p className="text-xs text-gray-500">No mutual users yet. If you only followed someone, ask them to follow you back first.</p>
              ) : (
                <div className="space-y-2">
                  {mutuals.map((user) => (
                    <label key={user.id} className="flex items-center gap-3 rounded-lg px-2 py-1.5 hover:bg-gray-50 cursor-pointer">
                      <input
                        type="checkbox"
                        checked={selectedEmails.includes(user.email)}
                        onChange={() => toggleSelection(user.email)}
                      />
                      <div>
                        <p className="text-sm font-medium text-gray-800">{user.firstname} {user.lastname}</p>
                        <p className="text-xs text-gray-500">{user.email}</p>
                      </div>
                    </label>
                  ))}
                </div>
              )}
            </div>
            <p className="mt-1 text-xs text-gray-400">Only users with mutual follow relationships can be added.</p>
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
              {loading ? "Creating..." : "Create group"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}