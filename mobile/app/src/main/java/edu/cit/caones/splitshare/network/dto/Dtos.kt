package edu.cit.caones.splitshare.network.dto

import com.google.gson.annotations.SerializedName

// ── Request DTOs ──────────────────────────────────────────────────────────────

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val firstname: String,
    val lastname: String,
    val email: String,
    val password: String
)

// ── Response DTOs ─────────────────────────────────────────────────────────────

data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val error: ApiError?,
    val timestamp: String?
)

data class ApiError(
    val code: String?,
    val message: String?,
    val details: Any?
)

data class AuthData(
    val user: UserDto,
    val accessToken: String,
    val refreshToken: String
)

data class UserDto(
    val email: String,
    val firstname: String,
    val lastname: String,
    val role: String?,
    val currency: String?
)

data class GroupSummaryDto(
    val id: Long,
    val name: String,
    val members: List<String>,
    val total: Double,
    val owed: Double?,
    val owe: Double?,
    val balance: Double,
    val createdAt: String
)

data class GroupMemberBalanceDto(
    val name: String,
    val email: String,
    val initial: String,
    val amount: Double,
    val positive: Boolean,
    val settlementPending: Boolean,
    val settledByCurrentUser: Boolean
)

data class ExpenseDto(
    val id: Long,
    val groupId: Long,
    val paidByEmail: String,
    val paidByName: String,
    val description: String,
    val category: String,
    val desc: String?,
    val sub: String?,
    val amount: Double,
    val share: Double,
    val positive: Boolean,
    val receiptUrl: String?,
    val createdAt: String
)

data class GroupDetailsDto(
    val id: Long,
    val name: String,
    val members: List<String>,
    val memberEmails: List<String>,
    val total: Double,
    val balance: Double,
    val createdAt: String,
    val balances: List<GroupMemberBalanceDto>,
    val expenses: List<ExpenseDto>
)

data class CreateGroupRequest(
    val name: String,
    val memberEmails: List<String>
)

data class UpdateGroupRequest(
    val name: String,
    val memberEmails: List<String>
)

data class CreateExpenseRequest(
    val description: String,
    val category: String,
    val amount: Double
)

data class UpdateExpenseRequest(
    val description: String,
    val category: String,
    val amount: Double
)

data class SettleBalanceRequest(
    val counterpartEmail: String
)

data class UserConnectionDto(
    val id: Long,
    val email: String,
    val firstname: String,
    val lastname: String,
    val following: Boolean,
    val followedBy: Boolean,
    val mutual: Boolean
)

data class UserActivityDto(
    val id: Long,
    val desc: String,
    val sub: String,
    val amount: Double,
    val share: Double,
    val positive: Boolean,
    val createdAt: String
)
