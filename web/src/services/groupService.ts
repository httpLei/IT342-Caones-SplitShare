import api from './api';
import type { ApiResponse } from '../types/auth';
import type { CreateExpenseRequest, CreateGroupRequest, ExpenseDto, GroupDetailsDto, GroupSummaryDto, UpdateExpenseRequest, UpdateGroupRequest } from '../types/groups';

function buildExpenseFormData(data: CreateExpenseRequest | UpdateExpenseRequest & { receipt?: File | null }) {
  const formData = new FormData();
  const { receipt, ...expense } = data as CreateExpenseRequest & { receipt?: File | null };
  formData.append('data', new Blob([JSON.stringify(expense)], { type: 'application/json' }));
  if (receipt) {
    formData.append('receipt', receipt);
  }
  return formData;
}

export const groupApi = {
  getGroups: () => api.get<ApiResponse<GroupSummaryDto[]>>('/groups'),
  createGroup: (data: CreateGroupRequest) => api.post<ApiResponse<GroupSummaryDto>>('/groups', data),
  getGroup: (groupId: number) => api.get<ApiResponse<GroupDetailsDto>>(`/groups/${groupId}`),
  updateGroup: (groupId: number, data: UpdateGroupRequest) => api.put<ApiResponse<GroupDetailsDto>>(`/groups/${groupId}`, data),
  addExpense: (groupId: number, data: CreateExpenseRequest & { receipt?: File | null }) =>
    api.post<ApiResponse<GroupDetailsDto>>(`/groups/${groupId}/expenses`, buildExpenseFormData(data)),
  updateExpense: (groupId: number, expenseId: number, data: UpdateExpenseRequest & { receipt?: File | null }) =>
    api.put<ApiResponse<GroupDetailsDto>>(`/groups/${groupId}/expenses/${expenseId}`, buildExpenseFormData(data)),
  deleteExpense: (groupId: number, expenseId: number) =>
    api.delete<ApiResponse<GroupDetailsDto>>(`/groups/${groupId}/expenses/${expenseId}`),
};

export const expenseApi = {
  getExpense: (expenseId: number) => api.get<ApiResponse<ExpenseDto>>(`/expenses/${expenseId}`),
  updateExpense: (expenseId: number, data: UpdateExpenseRequest & { receipt?: File | null }) =>
    api.put<ApiResponse<ExpenseDto>>(`/expenses/${expenseId}`, buildExpenseFormData(data)),
  deleteExpense: (expenseId: number) => api.delete<ApiResponse<string>>(`/expenses/${expenseId}`),
};