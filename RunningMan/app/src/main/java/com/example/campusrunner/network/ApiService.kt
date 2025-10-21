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

    /**
     * 功能：获取任务列表
     * 调用位置：TaskRepository.getTasksFromServer(), SearchRepository.searchTasks()
     * 后端实现示例：
     * GET /api/tasks?page=1&limit=20&type=FOOD_DELIVERY&search=麦当劳
     * 响应示例：
     * [
     *   {
     *     "id": "1",
     *     "title": "麦当劳大套餐",
     *     "description": "校门口取餐...",
     *     "price": 15.0,
     *     "type": "FOOD_DELIVERY",
     *     "status": "PENDING",
     *     ...
     *   }
     * ]
     */

    @GET("tasks/{id}")
    suspend fun getTaskDetail(@Path("id") taskId: String): Response<Task>

    /**
     * 功能：获取单个任务详情
     * 调用位置：TaskRepository.getTaskDetailFromServer()
     * 后端实现示例：
     * GET /api/tasks/1
     * 响应：单个Task对象的完整信息
     */

    @POST("tasks/{id}/accept")
    suspend fun acceptTask(@Path("id") taskId: String): Response<ApiResponse<String>>

    /**
     * 功能：接单操作
     * 调用位置：TaskRepository.acceptTask()
     * 后端实现示例：
     * POST /api/tasks/1/accept
     * 请求头：需要Authorization token
     * 响应示例：
     * {
     *   "code": 200,
     *   "message": "接单成功",
     *   "data": "订单已接受"
     * }
     */

    @POST("tasks")
    suspend fun createTask(@Body taskRequest: TaskRequest): Response<ApiResponse<String>>

    /**
     * 功能：发布新任务
     * 调用位置：TaskRepository.createTask()
     * 后端实现示例：
     * POST /api/tasks
     * 请求体：
     * {
     *   "title": "麦当劳大套餐",
     *   "description": "校门口取餐...",
     *   "price": 15.0,
     *   "type": "FOOD_DELIVERY",
     *   "location": "学校南门麦当劳",
     *   "destination": "图书馆三楼 A301",
     *   "estimatedTime": 15,
     *   "contactPhone": "138****1234"
     * }
     * 响应示例：
     * {
     *   "code": 200,
     *   "message": "任务发布成功",
     *   "data": "任务ID：12345"
     * }
     */

    // 消息相关API
    @GET("chats/sessions")
    suspend fun getChatSessions(): Response<List<ChatSession>>

    /**
     * 功能：获取用户的所有聊天会话列表
     * 调用位置：MessageRepository.getChatSessions()
     * 后端实现示例：
     * GET /api/chats/sessions
     * 响应示例：
     * [
     *   {
     *     "id": "chat1",
     *     "orderId": "1",
     *     "orderTitle": "麦当劳大套餐",
     *     "participantId": "runner1",
     *     "participantName": "张跑腿",
     *     "lastMessage": "我快到校门口了...",
     *     "unreadCount": 2,
     *     "lastMessageTime": "2024-01-20T10:30:00Z",
     *     "orderStatus": "IN_PROGRESS"
     *   }
     * ]
     */

    @GET("chats/{orderId}/messages")
    suspend fun getChatMessages(@Path("orderId") orderId: String): Response<List<ChatMessage>>

    /**
     * 功能：获取指定订单的聊天消息历史
     * 调用位置：MessageRepository.getChatMessages()
     * 后端实现示例：
     * GET /api/chats/1/messages
     * 响应示例：
     * [
     *   {
     *     "id": "msg1",
     *     "orderId": "1",
     *     "senderId": "runner1",
     *     "senderName": "张跑腿",
     *     "content": "我快到校门口了",
     *     "messageType": "TEXT",
     *     "timestamp": "2024-01-20T10:25:00Z",
     *     "isRead": false
     *   }
     * ]
     */

    @POST("chats/{orderId}/messages")
    suspend fun sendMessage(
        @Path("orderId") orderId: String,
        @Body messageRequest: MessageRequest
    ): Response<ApiResponse<String>>

    /**
     * 功能：发送聊天消息
     * 调用位置：MessageRepository.sendMessage()
     * 后端实现示例：
     * POST /api/chats/1/messages
     * 请求体：
     * {
     *   "content": "好的，我下去取",
     *   "type": "CHAT"
     * }
     * 响应示例：
     * {
     *   "code": 200,
     *   "message": "消息发送成功",
     *   "data": "消息ID：msg123"
     * }
     */

    @GET("messages/system")
    suspend fun getSystemMessages(): Response<List<Message>>

    /**
     * 功能：获取系统消息列表
     * 调用位置：MessageRepository.getSystemMessages()
     * 后端实现示例：
     * GET /api/messages/system
     * 响应示例：
     * [
     *   {
     *     "id": "sys1",
     *     "type": "ORDER_UPDATE",
     *     "title": "订单已完成",
     *     "content": "您的订单 #20240001 已完成",
     *     "senderId": "system",
     *     "senderName": "系统通知",
     *     "orderId": "2",
     *     "isRead": true,
     *     "createdAt": "2024-01-20T10:00:00Z"
     *   }
     * ]
     */

    // 实时订单相关API
    @GET("orders/current")
    suspend fun getCurrentOrders(): Response<List<LiveOrder>>

    /**
     * 功能：获取用户当前的实时订单（进行中的订单）
     * 调用位置：LiveOrderRepository.getCurrentLiveOrder()
     * 后端实现示例：
     * GET /api/orders/current
     * 响应示例：
     * [
     *   {
     *     "id": "live_1",
     *     "orderId": "1",
     *     "orderTitle": "麦当劳大套餐",
     *     "runnerId": "runner1",
     *     "runnerName": "张跑腿",
     *     "status": "ON_THE_WAY",
     *     "currentLocation": {
     *       "latitude": 30.5163,
     *       "longitude": 114.4204,
     *       "address": "华中科技大学南三门附近"
     *     },
     *     ...
     *   }
     * ]
     */

    @GET("orders/{orderId}/tracking")
    suspend fun getOrderTracking(@Path("orderId") orderId: String): Response<LiveOrder>

    /**
     * 功能：获取指定订单的实时跟踪信息
     * 调用位置：LiveOrderRepository.getOrderMapData()
     * 后端实现示例：
     * GET /api/orders/1/tracking
     * 响应：单个LiveOrder对象的完整信息
     */

    // 用户相关API
    @GET("user/profile")
    suspend fun getUserProfile(): Response<UserProfile>

    /**
     * 功能：获取当前登录用户的个人信息
     * 调用位置：UserRepository.login() 成功后调用，或需要更新用户信息时
     * 后端实现示例：
     * GET /api/user/profile
     * 响应示例：
     * {
     *   "id": "1",
     *   "studentId": "U202012345",
     *   "name": "小明",
     *   "avatar": "https://example.com/avatar.jpg",
     *   "phone": "138****1234",
     *   "email": "xiaoming@example.com",
     *   "creditScore": 4.8,
     *   "totalOrders": 8,
     *   "totalIncome": 85.0,
     *   "createdAt": "2024-01-01T00:00:00Z"
     * }
     */

    @PUT("user/profile")
    suspend fun updateUserProfile(@Body profile: UserProfile): Response<ApiResponse<String>>

    /**
     * 功能：更新用户个人信息
     * 调用位置：用户编辑个人信息时
     * 后端实现示例：
     * PUT /api/user/profile
     * 请求体：UserProfile对象（部分字段）
     * 响应示例：
     * {
     *   "code": 200,
     *   "message": "个人信息更新成功",
     *   "data": null
     * }
     */

    // 搜索相关API
    @GET("search/history")
    suspend fun getSearchHistory(
        @Query("userId") userId: String,
        @Query("limit") limit: Int = 10
    ): Response<SearchHistoryResponse>

    /**
     * 功能：获取用户的搜索历史记录
     * 调用位置：SearchRepository.getSearchHistory(), HomeViewModel.loadSearchHistory()
     * 后端实现示例：
     * GET /api/search/history?userId=1&limit=10
     * 响应示例：
     * {
     *   "histories": [
     *     {
     *       "id": "1",
     *       "userId": "1",
     *       "keyword": "麦当劳",
     *       "searchCount": 5,
     *       "lastSearchedAt": "2024-01-20T10:30:00Z",
     *       "createdAt": "2024-01-15T08:00:00Z"
     *     }
     *   ],
     *   "total": 15
     * }
     */

    @POST("search/history")
    suspend fun addSearchHistory(@Body request: SearchHistoryRequest): Response<ApiResponse<String>>

    /**
     * 功能：添加搜索历史记录
     * 调用位置：SearchRepository.addSearchHistory(), HomeViewModel.performSearch()
     * 后端实现示例：
     * POST /api/search/history
     * 请求体：
     * {
     *   "keyword": "麦当劳",
     *   "userId": "1"
     * }
     * 响应示例：
     * {
     *   "code": 200,
     *   "message": "搜索历史添加成功",
     *   "data": null
     * }
     */

    @DELETE("search/history/{id}")
    suspend fun deleteSearchHistory(@Path("id") historyId: String): Response<ApiResponse<String>>

    /**
     * 功能：删除单个搜索历史记录
     * 调用位置：SearchRepository.deleteSearchHistory(), HomeViewModel.deleteSearchHistory()
     * 后端实现示例：
     * DELETE /api/search/history/1
     * 响应示例：
     * {
     *   "code": 200,
     *   "message": "删除成功",
     *   "data": null
     * }
     */

    @DELETE("search/history")
    suspend fun clearSearchHistory(@Query("userId") userId: String): Response<ApiResponse<String>>

    /**
     * 功能：清空用户的所有搜索历史
     * 调用位置：SearchRepository.clearSearchHistory(), HomeViewModel.clearSearchHistory()
     * 后端实现示例：
     * DELETE /api/search/history?userId=1
     * 响应示例：
     * {
     *   "code": 200,
     *   "message": "搜索历史已清空",
     *   "data": null
     * }
     */

    // ===== 订单历史相关API =====

    @GET("orders/published")
    suspend fun getPublishedOrders(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
        @Query("status") status: String? = null
    ): Response<OrderListResponse>

    /**
     * 功能：获取用户发布的订单列表
     * 调用位置：OrderRepository.fetchPublishedOrders()
     * 后端实现示例：
     * GET /api/orders/published?page=1&pageSize=20&status=COMPLETED
     * 响应示例：
     * {
     *   "orders": [
     *     {
     *       "id": "1",
     *       "title": "麦当劳大套餐",
     *       "description": "校门口取餐...",
     *       "price": 15.0,
     *       "type": "外卖",
     *       "status": "COMPLETED",
     *       "createdAt": "2024-01-20T10:00:00Z",
     *       "updatedAt": "2024-01-20T10:30:00Z",
     *       "publisherId": "user1",
     *       "publisherName": "小明",
     *       "runnerId": "runner1",
     *       "runnerName": "张同学",
     *       "fromLocation": "校门口",
     *       "toLocation": "图书馆三楼 A301",
     *       "distance": 2.0,
     *       "estimatedTime": 15
     *     }
     *   ],
     *   "totalCount": 15,
     *   "page": 1,
     *   "pageSize": 20
     * }
     */

    @GET("orders/accepted")
    suspend fun getAcceptedOrders(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
        @Query("status") status: String? = null
    ): Response<OrderListResponse>

    /**
     * 功能：获取用户接单的订单列表
     * 调用位置：OrderRepository.fetchAcceptedOrders()
     * 后端实现示例：
     * GET /api/orders/accepted?page=1&pageSize=20&status=IN_PROGRESS
     * 响应格式：同getPublishedOrders
     */

    @GET("orders/{orderId}")
    suspend fun getOrderDetail(@Path("orderId") orderId: String): Response<Order>

    /**
     * 功能：获取订单详情
     * 调用位置：订单详情页面
     * 后端实现示例：
     * GET /api/orders/1
     * 响应：单个Order对象的完整信息
     */

    @GET("orders/stats")
    suspend fun getOrderStats(): Response<OrderStats>

    /**
     * 功能：获取用户订单统计信息
     * 调用位置：OrderRepository.fetchOrderStats()
     * 后端实现示例：
     * GET /api/orders/stats
     * 响应示例：
     * {
     *   "totalPublished": 15,
     *   "totalAccepted": 8,
     *   "totalCompleted": 6,
     *   "totalIncome": 120.0
     * }
     */

    @POST("orders/{orderId}/accept")
    suspend fun acceptOrder(@Path("orderId") orderId: String): Response<ApiResponse<Void>>

    /**
     * 功能：接单操作（订单历史相关）
     * 调用位置：OrderRepository.acceptOrder()
     * 后端实现示例：
     * POST /api/orders/1/accept
     * 响应示例：
     * {
     *   "code": 200,
     *   "message": "接单成功",
     *   "data": null
     * }
     */

    @POST("orders/{orderId}/complete")
    suspend fun completeOrder(@Path("orderId") orderId: String): Response<ApiResponse<Void>>

    /**
     * 功能：完成订单操作
     * 调用位置：OrderRepository.completeOrder()
     * 后端实现示例：
     * POST /api/orders/1/complete
     * 响应示例：
     * {
     *   "code": 200,
     *   "message": "订单已完成",
     *   "data": null
     * }
     */

    @POST("orders/{orderId}/cancel")
    suspend fun cancelOrder(@Path("orderId") orderId: String): Response<ApiResponse<Void>>

    /**
     * 功能：取消订单操作
     * 调用位置：OrderRepository.cancelOrder()
     * 后端实现示例：
     * POST /api/orders/1/cancel
     * 响应示例：
     * {
     *   "code": 200,
     *   "message": "订单已取消",
     *   "data": null
     * }
     */
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