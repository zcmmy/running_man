package com.example.campusrunner.network

import com.example.campusrunner.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // 任务相关API
    @GET("tasks")
    suspend fun getTasks(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("type") type: String? = null,
        @Query("location") location: String? = null,
        @Query("status") status: String? = null,
        @Query("search") search: String? = null // 新增搜索参数
    ): Response<List<Task>>

    @GET("tasks/{id}")
    suspend fun getTaskDetail(@Path("id") taskId: String): Response<Task>

    @POST("tasks/{id}/accept")
    suspend fun acceptTask(@Path("id") taskId: String): Response<ApiResponse<String>>

    @POST("tasks")
    suspend fun createTask(@Body taskRequest: TaskRequest): Response<ApiResponse<String>>

    // 消息相关API
    @GET("chats/sessions")
    suspend fun getChatSessions(): Response<List<ChatSession>>

    @GET("chats/{orderId}/messages")
    suspend fun getChatMessages(@Path("orderId") orderId: String): Response<List<ChatMessage>>

    @POST("chats/{orderId}/messages")
    suspend fun sendMessage(
        @Path("orderId") orderId: String,
        @Body messageRequest: MessageRequest
    ): Response<ApiResponse<String>>

    @GET("messages/system")
    suspend fun getSystemMessages(): Response<List<Message>>

    // 实时订单相关API
    @GET("orders/current")
    suspend fun getCurrentOrders(): Response<List<LiveOrder>>

    @GET("orders/{orderId}/tracking")
    suspend fun getOrderTracking(@Path("orderId") orderId: String): Response<LiveOrder>

    // 用户相关API
    @GET("user/profile")
    suspend fun getUserProfile(): Response<UserProfile>

    @PUT("user/profile")
    suspend fun updateUserProfile(@Body profile: UserProfile): Response<ApiResponse<String>>

    // 搜索相关API - 新增
    @GET("search/history")
    suspend fun getSearchHistory(
        @Query("userId") userId: String,
        @Query("limit") limit: Int = 10
    ): Response<SearchHistoryResponse>

    @POST("search/history")
    suspend fun addSearchHistory(@Body request: SearchHistoryRequest): Response<ApiResponse<String>>

    @DELETE("search/history/{id}")
    suspend fun deleteSearchHistory(@Path("id") historyId: String): Response<ApiResponse<String>>

    @DELETE("search/history")
    suspend fun clearSearchHistory(@Query("userId") userId: String): Response<ApiResponse<String>>
}

// 请求和响应数据类
data class ApiResponse<T>(
    val code: Int,
    val message: String,
    val data: T? = null
)

data class TaskRequest(
    val title: String,
    val description: String,
    val price: Double,
    val type: TaskType,
    val location: String,
    val destination: String,
    val estimatedTime: Int? = null,
    val contactPhone: String? = null,
    val specialRequirements: String? = null
)

data class MessageRequest(
    val content: String,
    val type: MessageType = MessageType.CHAT
)