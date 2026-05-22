import axios from 'axios';
import { API_BASE_URL } from './config';
import type {
  AdminAuditLogDto,
  AdminUserDto,
  ApiResponse,
  AuthData,
  LoginRequest,
  RegisterRequest,
} from '../../features/auth/types/auth';

const api = axios.create({
  baseURL: `${API_BASE_URL}/api/v1`,
  headers: { 'Content-Type': 'application/json' },
});

// Attach JWT to every request when available
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) config.headers.Authorization = `Bearer ${token}`;

  // For FormData requests, delete the Content-Type header so Axios auto-sets it with proper boundary
  if (config.data instanceof FormData) {
    delete config.headers['Content-Type'];
  }

  return config;
});

// Auto-logout on 401 so stale/expired tokens don't leave the user stuck
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export const authApi = {
  register: (data: RegisterRequest) =>
    api.post<ApiResponse<AuthData>>('/auth/register', data),

  login: (data: LoginRequest) =>
    api.post<ApiResponse<AuthData>>('/auth/login', data),
};

export const adminApi = {
  getUsers: () =>
    api.get<ApiResponse<AdminUserDto[]>>('/admin/users'),

  updateUserStatus: (id: number, enabled: boolean) =>
    api.put<ApiResponse<AdminUserDto>>(`/admin/users/${id}/status`, { enabled }),

  getAuditLogs: (limit = 50) =>
    api.get<ApiResponse<AdminAuditLogDto[]>>('/admin/audit-logs', { params: { limit } }),
};

export default api;
