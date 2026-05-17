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

data class UserActivityDto(
    val id: Long,
    val desc: String,
    val sub: String,
    val amount: Double,
    val share: Double,
    val positive: Boolean,
    val createdAt: String
)
