import api from './api';
import type { ApiResponse } from '../types/auth';
import type { UserActivityDto, UserConnectionDto } from '../types/social';

export const userApi = {
  search: (query: string) => api.get<ApiResponse<UserConnectionDto[]>>('/users/search', { params: { q: query } }),
  getMutuals: () => api.get<ApiResponse<UserConnectionDto[]>>('/users/mutuals'),
  getMyHistory: () => api.get<ApiResponse<UserActivityDto[]>>('/users/me/history'),
  follow: (id: number) => api.post<ApiResponse<string>>(`/users/${id}/follow`),
  unfollow: (id: number) => api.delete<ApiResponse<string>>(`/users/${id}/follow`),
};