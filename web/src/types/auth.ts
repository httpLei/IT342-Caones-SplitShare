export interface RegisterRequest {
  firstname: string;
  lastname: string;
  email: string;
  password: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface UserDto {
  email: string;
  firstname: string;
  lastname: string;
  role?: string;
}

export interface AuthData {
  user: UserDto;
  accessToken: string;
  refreshToken: string;
}

export interface ApiError {
  code: string;
  message: string;
  details: unknown;
}

export interface ApiResponse<T> {
  success: boolean;
  data: T | null;
  error: ApiError | null;
  timestamp: string;
}

export interface AdminUserDto {
  id: number;
  email: string;
  firstname: string;
  lastname: string;
  role: string;
  enabled: boolean;
  createdAt: string;
}

export interface AdminAuditLogDto {
  id: number;
  action: string;
  actorEmail: string;
  targetUserId: number | null;
  targetUserEmail: string | null;
  details: string | null;
  createdAt: string;
}
