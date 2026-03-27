import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { adminApi } from '../services/api';
import { useAuth } from '../context/AuthContext';
import type { AdminAuditLogDto, AdminUserDto } from '../types/auth';

function formatDate(value: string) {
  if (!value) return '-';
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) return value;
  return d.toLocaleString();
}

export default function Admin() {
  const navigate = useNavigate();
  const { logout } = useAuth();
  const [users, setUsers] = useState<AdminUserDto[]>([]);
  const [logs, setLogs] = useState<AdminAuditLogDto[]>([]);
  const [loadingUsers, setLoadingUsers] = useState(true);
  const [loadingLogs, setLoadingLogs] = useState(true);
  const [actionId, setActionId] = useState<number | null>(null);
  const [error, setError] = useState('');
  const [showLogoutModal, setShowLogoutModal] = useState(false);

  const confirmLogout = () => {
    setShowLogoutModal(false);
    logout();
    navigate('/login', { replace: true });
  };

  const activeUsers = useMemo(() => users.filter((u) => u.enabled).length, [users]);
  const suspendedUsers = useMemo(() => users.filter((u) => !u.enabled).length, [users]);

  const fetchUsers = async () => {
    setLoadingUsers(true);
    try {
      const res = await adminApi.getUsers();
      if (res.data.success && res.data.data) {
        setUsers(res.data.data);
      } else {
        setError(res.data.error?.message ?? 'Failed to load users.');
      }
    } catch (err: unknown) {
      if (axios.isAxiosError(err) && err.response?.data) {
        setError(err.response.data.error?.message ?? 'Failed to load users.');
      } else {
        setError('Unable to reach server. Is the backend running?');
      }
    } finally {
      setLoadingUsers(false);
    }
  };

  const fetchLogs = async () => {
    setLoadingLogs(true);
    try {
      const res = await adminApi.getAuditLogs(50);
      if (res.data.success && res.data.data) {
        setLogs(res.data.data);
      } else {
        setError(res.data.error?.message ?? 'Failed to load audit logs.');
      }
    } catch (err: unknown) {
      if (axios.isAxiosError(err) && err.response?.data) {
        setError(err.response.data.error?.message ?? 'Failed to load audit logs.');
      } else {
        setError('Unable to reach server. Is the backend running?');
      }
    } finally {
      setLoadingLogs(false);
    }
  };

  useEffect(() => {
    fetchUsers();
    fetchLogs();
  }, []);

  const onToggleUser = async (user: AdminUserDto) => {
    setActionId(user.id);
    setError('');
    try {
      const res = await adminApi.updateUserStatus(user.id, !user.enabled);
      if (!res.data.success || !res.data.data) {
        setError(res.data.error?.message ?? 'Unable to update user status.');
        return;
      }

      setUsers((prev) => prev.map((u) => (u.id === user.id ? res.data.data! : u)));
      fetchLogs();
    } catch (err: unknown) {
      if (axios.isAxiosError(err) && err.response?.data) {
        setError(err.response.data.error?.message ?? 'Unable to update user status.');
      } else {
        setError('Unable to reach server. Is the backend running?');
      }
    } finally {
      setActionId(null);
    }
  };

  return (
    <div className="min-h-screen px-6 py-8 md:px-10" style={{ background: '#f4f1fb' }}>
      <div className="max-w-6xl mx-auto space-y-6">
        <div className="flex items-start justify-between gap-4">
          <div>
            <p className="text-xs font-bold tracking-widest uppercase" style={{ color: '#8f7bb5' }}>
              Admin Console
            </p>
            <h1 className="text-3xl font-bold text-gray-900 mt-1">User Management</h1>
            <p className="text-sm text-gray-500 mt-2">
              Suspend or reactivate accounts and inspect recent admin actions.
            </p>
          </div>
          <button
            onClick={() => setShowLogoutModal(true)}
            className="px-4 py-2 text-sm font-bold rounded-lg text-white"
            style={{ background: '#b42318' }}
          >
            Logout
          </button>
        </div>

        {error && (
          <div className="text-sm rounded-lg px-4 py-3" style={{ background: '#fff1f2', color: '#b42318' }}>
            {error}
          </div>
        )}

        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div className="rounded-2xl p-5 border border-gray-100 bg-white shadow-sm">
            <p className="text-xs uppercase tracking-wide text-gray-500">Total users</p>
            <p className="text-2xl font-bold text-gray-900 mt-2">{users.length}</p>
          </div>
          <div className="rounded-2xl p-5 border border-gray-100 bg-white shadow-sm">
            <p className="text-xs uppercase tracking-wide text-gray-500">Active</p>
            <p className="text-2xl font-bold mt-2" style={{ color: '#15803d' }}>{activeUsers}</p>
          </div>
          <div className="rounded-2xl p-5 border border-gray-100 bg-white shadow-sm">
            <p className="text-xs uppercase tracking-wide text-gray-500">Suspended</p>
            <p className="text-2xl font-bold mt-2" style={{ color: '#b42318' }}>{suspendedUsers}</p>
          </div>
        </div>

        <section className="rounded-2xl border border-gray-100 bg-white shadow-sm overflow-hidden">
          <div className="px-5 py-4 border-b border-gray-100 flex items-center justify-between">
            <h2 className="text-base font-bold text-gray-900">Users</h2>
            <button
              onClick={fetchUsers}
              className="text-sm font-semibold px-3 py-1.5 rounded-lg border border-gray-200 hover:bg-gray-50 cursor-pointer"
            >
              Refresh
            </button>
          </div>

          {loadingUsers ? (
            <p className="px-5 py-8 text-sm text-gray-500">Loading users...</p>
          ) : users.length === 0 ? (
            <p className="px-5 py-8 text-sm text-gray-500">No users found.</p>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full min-w-[700px]">
                <thead>
                  <tr className="text-left text-xs uppercase tracking-wide text-gray-500 bg-gray-50">
                    <th className="px-5 py-3">User</th>
                    <th className="px-5 py-3">Role</th>
                    <th className="px-5 py-3">Status</th>
                    <th className="px-5 py-3">Created</th>
                    <th className="px-5 py-3">Action</th>
                  </tr>
                </thead>
                <tbody>
                  {users.map((u) => (
                    <tr key={u.id} className="border-t border-gray-100 text-sm">
                      <td className="px-5 py-3">
                        <p className="font-semibold text-gray-900">{u.firstname} {u.lastname}</p>
                        <p className="text-xs text-gray-500">{u.email}</p>
                      </td>
                      <td className="px-5 py-3 text-gray-700">{u.role}</td>
                      <td className="px-5 py-3">
                        <span
                          className="text-xs font-bold px-2 py-1 rounded-full"
                          style={{
                            background: u.enabled ? '#ecfdf3' : '#fff1f2',
                            color: u.enabled ? '#15803d' : '#b42318',
                          }}
                        >
                          {u.enabled ? 'ACTIVE' : 'SUSPENDED'}
                        </span>
                      </td>
                      <td className="px-5 py-3 text-gray-600">{formatDate(u.createdAt)}</td>
                      <td className="px-5 py-3">
                        <button
                          onClick={() => onToggleUser(u)}
                          disabled={actionId === u.id}
                          className="px-3 py-1.5 rounded-lg text-xs font-bold text-white cursor-pointer disabled:opacity-60"
                          style={{ background: u.enabled ? '#b42318' : '#15803d' }}
                        >
                          {actionId === u.id
                            ? 'Updating...'
                            : u.enabled
                              ? 'Suspend'
                              : 'Reactivate'}
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>

        <section className="rounded-2xl border border-gray-100 bg-white shadow-sm overflow-hidden">
          <div className="px-5 py-4 border-b border-gray-100 flex items-center justify-between">
            <h2 className="text-base font-bold text-gray-900">Recent Admin Audit Logs</h2>
            <button
              onClick={fetchLogs}
              className="text-sm font-semibold px-3 py-1.5 rounded-lg border border-gray-200 hover:bg-gray-50 cursor-pointer"
            >
              Refresh
            </button>
          </div>

          {loadingLogs ? (
            <p className="px-5 py-8 text-sm text-gray-500">Loading logs...</p>
          ) : logs.length === 0 ? (
            <p className="px-5 py-8 text-sm text-gray-500">No admin actions recorded yet.</p>
          ) : (
            <div className="divide-y divide-gray-100">
              {logs.map((log) => (
                <div key={log.id} className="px-5 py-4 text-sm flex flex-col md:flex-row md:items-center md:justify-between gap-2">
                  <div>
                    <p className="font-semibold text-gray-900">{log.action}</p>
                    <p className="text-xs text-gray-500">
                      Actor: {log.actorEmail} | Target: {log.targetUserEmail ?? 'N/A'}
                    </p>
                    {log.details && <p className="text-xs text-gray-500 mt-1">{log.details}</p>}
                  </div>
                  <p className="text-xs text-gray-500">{formatDate(log.createdAt)}</p>
                </div>
              ))}
            </div>
          )}
        </section>

        {/* Logout confirmation modal */}
        {showLogoutModal && (
          <div className="fixed inset-0 z-50 flex items-center justify-center"
               style={{ background: "rgba(0,0,0,0.5)" }}
               onClick={() => setShowLogoutModal(false)}>
            <div className="bg-white rounded-2xl shadow-2xl p-8 w-full max-w-sm mx-4"
                 onClick={e => e.stopPropagation()}>
              <div className="flex items-center justify-center w-14 h-14 rounded-full mx-auto mb-5"
                   style={{ background: "#f5f0ff" }}>
                <svg width="28" height="28" viewBox="0 0 24 24" fill="none">
                  <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" stroke="#662498" strokeWidth="2" strokeLinecap="round"/>
                  <polyline points="16 17 21 12 16 7" stroke="#662498" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                  <line x1="21" y1="12" x2="9" y2="12" stroke="#662498" strokeWidth="2" strokeLinecap="round"/>
                </svg>
              </div>
              <h3 className="text-lg font-bold text-gray-900 text-center">Sign out?</h3>
              <p className="text-sm text-gray-400 text-center mt-1 mb-7">
                Are you sure you want to sign out of SplitShare?
              </p>
              <div className="flex gap-3">
                <button
                  onClick={() => setShowLogoutModal(false)}
                  className="flex-1 py-2.5 rounded-xl text-sm font-semibold border border-gray-200 text-gray-600 hover:bg-gray-50 transition cursor-pointer"
                >
                  Cancel
                </button>
                <button
                  onClick={confirmLogout}
                  className="flex-1 py-2.5 rounded-xl text-sm font-bold text-white transition cursor-pointer"
                  style={{ background: "#662498" }}
                  onMouseEnter={e => ((e.currentTarget as HTMLElement).style.background = "#4a1870")}
                  onMouseLeave={e => ((e.currentTarget as HTMLElement).style.background = "#662498")}
                >
                  Sign out
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
