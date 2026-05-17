package edu.cit.caones.splitshare.network

import edu.cit.caones.splitshare.network.dto.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface SplitShareApi {

    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<AuthData>>

    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<AuthData>>

    @GET("api/v1/groups")
    suspend fun getGroups(): Response<ApiResponse<List<GroupSummaryDto>>>

    @GET("api/v1/users/me/history")
    suspend fun getMyHistory(): Response<ApiResponse<List<UserActivityDto>>>
}
