export interface GroupSummaryDto {
  id: number;
  name: string;
  members: string[];
  total: number;
  balance: number;
  createdAt: string;
}

export interface GroupMemberBalanceDto {
  name: string;
  initial: string;
  amount: number;
  positive: boolean;
}

export interface ExpenseDto {
  id: number;
  groupId: number;
  description: string;
  category: string;
  desc: string;
  sub: string;
  amount: number;
  share: number;
  positive: boolean;
  receiptUrl?: string | null;
  createdAt: string;
}

export interface GroupDetailsDto extends GroupSummaryDto {
  memberEmails?: string[];
  balances: GroupMemberBalanceDto[];
  expenses: ExpenseDto[];
}

export interface CreateGroupRequest {
  name: string;
  memberEmails: string[];
}

export interface UpdateGroupRequest {
  name?: string;
  memberEmails: string[];
}

export interface CreateExpenseRequest {
  description: string;
  category: string;
  amount: number;
}

export interface UpdateExpenseRequest {
  description: string;
  category: string;
  amount: number;
}