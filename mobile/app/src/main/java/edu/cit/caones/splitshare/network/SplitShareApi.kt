package edu.cit.caones.splitshare.network

import edu.cit.caones.splitshare.network.dto.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.PUT
import retrofit2.http.Query
import okhttp3.MultipartBody
import okhttp3.RequestBody

interface SplitShareApi {

    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<AuthData>>

    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<AuthData>>

    @GET("api/v1/groups")
    suspend fun getGroups(): Response<ApiResponse<List<GroupSummaryDto>>>

    @GET("api/v1/groups/{groupId}")
    suspend fun getGroup(@Path("groupId") groupId: Long): Response<ApiResponse<GroupDetailsDto>>

    @POST("api/v1/groups")
    suspend fun createGroup(@Body request: CreateGroupRequest): Response<ApiResponse<GroupSummaryDto>>

    @PUT("api/v1/groups/{groupId}")
    suspend fun updateGroup(@Path("groupId") groupId: Long, @Body request: UpdateGroupRequest): Response<ApiResponse<GroupDetailsDto>>

    @DELETE("api/v1/groups/{groupId}")
    suspend fun deleteGroup(@Path("groupId") groupId: Long): Response<ApiResponse<String>>

    @Multipart
    @POST("api/v1/groups/{groupId}/expenses")
    suspend fun addExpense(
        @Path("groupId") groupId: Long,
        @Part("data") data: RequestBody,
        @Part receipt: MultipartBody.Part? = null,
    ): Response<ApiResponse<GroupDetailsDto>>

    @POST("api/v1/groups/{groupId}/settlements")
    suspend fun settleBalance(@Path("groupId") groupId: Long, @Body request: SettleBalanceRequest): Response<ApiResponse<GroupDetailsDto>>

    @GET("api/v1/expenses/{expenseId}")
    suspend fun getExpense(@Path("expenseId") expenseId: Long): Response<ApiResponse<ExpenseDto>>

    @Multipart
    @PUT("api/v1/expenses/{expenseId}")
    suspend fun updateExpense(
        @Path("expenseId") expenseId: Long,
        @Part("data") data: RequestBody,
        @Part receipt: MultipartBody.Part? = null,
    ): Response<ApiResponse<ExpenseDto>>

    @DELETE("api/v1/expenses/{expenseId}")
    suspend fun deleteExpense(@Path("expenseId") expenseId: Long): Response<ApiResponse<String>>

    @GET("api/v1/users/search")
    suspend fun searchUsers(@Query("q") query: String): Response<ApiResponse<List<UserConnectionDto>>>

    @GET("api/v1/users/mutuals")
    suspend fun getMutuals(): Response<ApiResponse<List<UserConnectionDto>>>

    @POST("api/v1/users/{id}/follow")
    suspend fun followUser(@Path("id") userId: Long): Response<ApiResponse<String>>

    @DELETE("api/v1/users/{id}/follow")
    suspend fun unfollowUser(@Path("id") userId: Long): Response<ApiResponse<String>>

    @GET("api/v1/users/me/history")
    suspend fun getMyHistory(): Response<ApiResponse<List<UserActivityDto>>>
}
