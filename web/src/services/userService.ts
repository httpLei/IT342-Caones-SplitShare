import api from './api';
import type { ApiResponse, UserDto } from '../types/auth';
import type { UserActivityDto, UserConnectionDto, UserProfileStatsDto } from '../types/social';

export const userApi = {
  search: (query: string) => api.get<ApiResponse<UserConnectionDto[]>>('/users/search', { params: { q: query } }),
  getMutuals: () => api.get<ApiResponse<UserConnectionDto[]>>('/users/mutuals'),
  getFollowers: () => api.get<ApiResponse<UserConnectionDto[]>>('/users/me/followers'),
  getFollowing: () => api.get<ApiResponse<UserConnectionDto[]>>('/users/me/following'),
  getMyHistory: () => api.get<ApiResponse<UserActivityDto[]>>('/users/me/history'),
  getProfileStats: () => api.get<ApiResponse<UserProfileStatsDto>>('/users/me/stats'),
  updateProfile: (firstname: string, lastname: string) =>
    api.put<ApiResponse<UserDto>>('/users/me', { firstname, lastname }),
  follow: (id: number) => api.post<ApiResponse<string>>(`/users/${id}/follow`),
  unfollow: (id: number) => api.delete<ApiResponse<string>>(`/users/${id}/follow`),
};