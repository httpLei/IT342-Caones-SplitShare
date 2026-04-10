import api from './api';
import type { ApiResponse } from '../types/auth';
import type { CreateExpenseRequest, CreateGroupRequest, GroupDetailsDto, GroupSummaryDto } from '../types/groups';

export const groupApi = {
  getGroups: () => api.get<ApiResponse<GroupSummaryDto[]>>('/groups'),
  createGroup: (data: CreateGroupRequest) => api.post<ApiResponse<GroupSummaryDto>>('/groups', data),
  getGroup: (groupId: number) => api.get<ApiResponse<GroupDetailsDto>>(`/groups/${groupId}`),
  addExpense: (groupId: number, data: CreateExpenseRequest) =>
    api.post<ApiResponse<GroupDetailsDto>>(`/groups/${groupId}/expenses`, data),
};