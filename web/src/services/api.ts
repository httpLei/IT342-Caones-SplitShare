import axios from 'axios';
import type { ApiResponse, AuthData, LoginRequest, RegisterRequest } from '../types/auth';

const api = axios.create({
  baseURL: '/api/v1',
  headers: { 'Content-Type': 'application/json' },
});

// Attach JWT to every request when available
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

export const authApi = {
  register: (data: RegisterRequest) =>
    api.post<ApiResponse<AuthData>>('/auth/register', data),

  login: (data: LoginRequest) =>
    api.post<ApiResponse<AuthData>>('/auth/login', data),
};

export default api;
