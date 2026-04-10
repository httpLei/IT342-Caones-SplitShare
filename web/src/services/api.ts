import axios from 'axios';
import type {
  AdminAuditLogDto,
  AdminUserDto,
  ApiResponse,
  AuthData,
  LoginRequest,
  RegisterRequest,
} from '../types/auth';

const api = axios.create({
  baseURL: '/api/v1',
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
