import { useEffect, useState, type FormEvent } from "react";
import type { UserConnectionDto } from "../types/social";

type Member = {
  name: string;
  email: string;
};

type Props = {
  open: boolean;
  loading?: boolean;
  error?: string;
  groupName: string;
  currentMembers: Member[];
  mutuals: UserConnectionDto[];
  onClose: () => void;
  onSubmit: (payload: { name: string; memberEmails: string[] }) => void;
};

export default function EditGroupModal({
  open,
  loading = false,
  error = "",
  groupName,
  currentMembers,
  mutuals,
  onClose,
  onSubmit,
}: Props) {
  const [name, setName] = useState(groupName);
  const [selectedEmails, setSelectedEmails] = useState<string[]>([]);

  useEffect(() => {
    if (!open) {
      setName(groupName);
      setSelectedEmails([]);
    } else {
      setName(groupName);
    }
  }, [open, groupName]);

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
            <h3 className="text-xl font-bold text-gray-900">Edit group</h3>
            <p className="text-sm text-gray-500 mt-1">Rename the group or add more mutual members.</p>
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
            <label className="mb-1.5 block text-xs font-semibold uppercase tracking-wide text-gray-500">Current members</label>
            <div className="rounded-xl border border-gray-200 p-3">
              {currentMembers.length === 0 ? (
                <p className="text-xs text-gray-500">No members available.</p>
              ) : (
                <div className="flex flex-wrap gap-2">
                  {currentMembers.map((member) => (
                    <span key={member.email || member.name} className="rounded-full bg-gray-100 px-3 py-1 text-xs font-medium text-gray-700">
                      {member.name}
                    </span>
                  ))}
                </div>
              )}
            </div>
          </div>

          <div>
            <label className="mb-1.5 block text-xs font-semibold uppercase tracking-wide text-gray-500">Add more mutual users</label>
            <div className="max-h-48 overflow-y-auto rounded-xl border border-gray-200 p-3">
              {mutuals.length === 0 ? (
                <p className="text-xs text-gray-500">No mutual users yet.</p>
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
            <p className="mt-1 text-xs text-gray-400">Only mutual users can be added to the group.</p>
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
              {loading ? "Saving..." : "Save changes"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}