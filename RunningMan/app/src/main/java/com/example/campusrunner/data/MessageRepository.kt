package com.example.campusrunner.data

import com.example.campusrunner.model.ChatMessage
import com.example.campusrunner.model.ChatMessageType
import com.example.campusrunner.model.ChatSession
import com.example.campusrunner.model.Message
import com.example.campusrunner.model.MessageType
import com.example.campusrunner.model.TaskStatus
import com.example.campusrunner.network.ApiService
import com.example.campusrunner.network.RetrofitClient
import java.util.Date

/**
 * 消息数据仓库
 */
object MessageRepository {

    // 原有的模拟数据保持不变
    private val mockChatSessions = listOf(
        ChatSession(
            id = "chat1",
            orderId = "1",
            orderTitle = "麦当劳大套餐",
            participantId = "runner1",
            participantName = "张跑腿",
            lastMessage = "我快到校门口了，大约5分钟",
            unreadCount = 2,
            lastMessageTime = Date(System.currentTimeMillis() - 5 * 60 * 1000),
            orderStatus = TaskStatus.IN_PROGRESS
        ),
        ChatSession(
            id = "chat2",
            orderId = "2",
            orderTitle = "打印20页文档",
            participantId = "runner2",
            participantName = "李跑腿",
            lastMessage = "文件已经打印好了，正在送往教学楼",
            unreadCount = 0,
            lastMessageTime = Date(System.currentTimeMillis() - 30 * 60 * 1000),
            orderStatus = TaskStatus.COMPLETED
        ),
        ChatSession(
            id = "chat3",
            orderId = "3",
            orderTitle = "取快递包裹",
            participantId = "runner3",
            participantName = "王跑腿",
            lastMessage = "快递点排队人比较多，可能需要稍等一会儿",
            unreadCount = 1,
            lastMessageTime = Date(System.currentTimeMillis() - 2 * 60 * 60 * 1000),
            orderStatus = TaskStatus.IN_PROGRESS
        )
    )

    private val mockSystemMessages = listOf(
        Message(
            id = "sys1",
            type = MessageType.ORDER_UPDATE,
            title = "订单已完成",
            content = "您的订单 #20240001 已完成，感谢使用RunningMan服务",
            senderId = "system",
            senderName = "系统通知",
            orderId = "2",
            isRead = true,
            createdAt = Date(System.currentTimeMillis() - 25 * 60 * 1000)
        ),
        Message(
            id = "sys2",
            type = MessageType.SYSTEM,
            title = "新功能上线",
            content = "RunningMan新增实时位置追踪功能，快来体验吧！",
            senderId = "system",
            senderName = "系统通知",
            isRead = false,
            createdAt = Date(System.currentTimeMillis() - 3 * 60 * 60 * 1000)
        ),
        Message(
            id = "sys3",
            type = MessageType.PROMOTION,
            title = "优惠活动",
            content = "新用户首单立减5元，快来发布任务吧！",
            senderId = "system",
            senderName = "活动推广",
            isRead = false,
            createdAt = Date(System.currentTimeMillis() - 6 * 60 * 60 * 1000)
        )
    )

    // 模拟聊天消息数据
    private val mockChatMessages = mapOf(
        "1" to listOf(
            ChatMessage(
                id = "1",
                orderId = "1",
                senderId = "runner1",
                senderName = "张跑腿",
                content = "我快到校门口了。",
                messageType = ChatMessageType.TEXT,
                timestamp = Date(System.currentTimeMillis() - 10 * 60 * 1000)
            ),
            ChatMessage(
                id = "2",
                orderId = "1",
                senderId = "current_user",
                senderName = "您",
                content = "好的，我下去取。",
                messageType = ChatMessageType.TEXT,
                timestamp = Date(System.currentTimeMillis() - 8 * 60 * 1000)
            ),
            ChatMessage(
                id = "3",
                orderId = "1",
                senderId = "runner1",
                senderName = "张跑腿",
                content = "大约 5 分钟。",
                messageType = ChatMessageType.TEXT,
                timestamp = Date(System.currentTimeMillis() - 5 * 60 * 1000)
            )
        ),
        "2" to listOf(
            ChatMessage(
                id = "4",
                orderId = "2",
                senderId = "runner2",
                senderName = "李跑腿",
                content = "文件已经打印好了，正在送往教学楼",
                messageType = ChatMessageType.TEXT,
                timestamp = Date(System.currentTimeMillis() - 25 * 60 * 1000)
            )
        ),
        "3" to listOf(
            ChatMessage(
                id = "5",
                orderId = "3",
                senderId = "runner3",
                senderName = "王跑腿",
                content = "快递点排队人比较多，可能需要稍等一会儿",
                messageType = ChatMessageType.TEXT,
                timestamp = Date(System.currentTimeMillis() - 120 * 60 * 1000)
            )
        )
    )

    private val apiService: ApiService = RetrofitClient.apiService

    /**
     * 功能：获取聊天会话列表
     * 后端接入步骤：
     * 1. 取消注释apiService.getChatSessions()调用
     * 2. 处理网络响应
     * 3. 移除模拟数据逻辑
     * 调用位置：MessagesScreen, 消息页面
     */
    fun getChatSessions(
        onSuccess: (List<ChatSession>) -> Unit,
        onError: (String) -> Unit
    ) {
        Thread {
            Thread.sleep(300)

            // TODO: 当后端API准备好后，取消注释以下代码
            /*
            try {
                val response = apiService.getChatSessions().execute()
                if (response.isSuccessful) {
                    onSuccess(response.body() ?: emptyList())
                } else {
                    onError("获取聊天会话失败: ${response.code()}")
                }
            } catch (e: Exception) {
                onError("网络错误: ${e.message}")
            }
            */

            // 临时使用模拟数据 - 后端API完成后删除
            onSuccess(mockChatSessions)
        }.start()
    }

    /**
     * 功能：获取聊天消息历史
     * 后端接入步骤：
     * 1. 取消注释apiService.getChatMessages()调用
     * 2. 处理网络响应
     * 3. 移除模拟数据逻辑
     * 调用位置：ChatScreen, 聊天页面
     */
    fun getChatMessages(
        orderId: String,
        onSuccess: (List<ChatMessage>) -> Unit,
        onError: (String) -> Unit
    ) {
        Thread {
            Thread.sleep(300)

            // TODO: 当后端API准备好后，取消注释以下代码
            /*
            try {
                val response = apiService.getChatMessages(orderId).execute()
                if (response.isSuccessful) {
                    onSuccess(response.body() ?: emptyList())
                } else {
                    onError("获取聊天消息失败: ${response.code()}")
                }
            } catch (e: Exception) {
                onError("网络错误: ${e.message}")
            }
            */

            // 临时使用模拟数据 - 后端API完成后删除
            val messages = mockChatMessages[orderId] ?: emptyList()
            onSuccess(messages)
        }.start()
    }

    /**
     * 获取系统消息列表
     * 后端接入步骤：
     * 1. 取消注释 apiService.getSystemMessages().execute() 调用
     * 2. 处理网络响应，包括成功和错误情况
     * 3. 移除模拟数据逻辑
     * 调用位置：MessagesScreen.kt, 系统消息页面
     *
     * 后端API实现示例：
     * GET /api/messages/system
     * 请求示例：GET /api/messages/system
     * 响应示例：
     * [
     *   {
     *     "id": "sys1",
     *     "type": "ORDER_UPDATE",
     *     "title": "订单已完成",
     *     "content": "您的订单 #20240001 已完成，感谢使用RunningMan服务",
     *     "senderId": "system",
     *     "senderName": "系统通知",
     *     "orderId": "2",
     *     "isRead": true,
     *     "createdAt": "2024-01-20T10:00:00Z"
     *   },
     *   {
     *     "id": "sys2",
     *     "type": "SYSTEM",
     *     "title": "新功能上线",
     *     "content": "RunningMan新增实时位置追踪功能，快来体验吧！",
     *     "senderId": "system",
     *     "senderName": "系统通知",
     *     "isRead": false,
     *     "createdAt": "2024-01-20T09:30:00Z"
     *   }
     * ]
     */
    fun getSystemMessages(
        onSuccess: (List<Message>) -> Unit,
        onError: (String) -> Unit
    ) {
        Thread {
            Thread.sleep(300)

            // TODO: 当后端API准备好后，取消注释以下代码
            /*
            try {
                val response = apiService.getSystemMessages().execute()
                if (response.isSuccessful) {
                    onSuccess(response.body() ?: emptyList())
                } else {
                    onError("获取系统消息失败: ${response.code()}")
                }
            } catch (e: Exception) {
                onError("网络错误: ${e.message}")
            }
            */

            // 临时使用模拟数据 - 后端API完成后删除
            onSuccess(mockSystemMessages)
        }.start()
    }

    /**
     * 功能：发送聊天消息
     * 后端接入步骤：
     * 1. 取消注释apiService.sendMessage()调用
     * 2. 处理网络响应
     * 3. 移除模拟成功逻辑
     * 调用位置：ChatScreen, 用户发送消息时
     */
    fun sendMessage(
        orderId: String,
        content: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        Thread {
            Thread.sleep(500)

            // TODO: 当后端API准备好后，取消注释以下代码
            /*
            try {
                val response = apiService.sendMessage(orderId, MessageRequest(content)).execute()
                if (response.isSuccessful && response.body()?.code == 200) {
                    onSuccess()
                } else {
                    onError("发送消息失败: ${response.body()?.message ?: "未知错误"}")
                }
            } catch (e: Exception) {
                onError("网络错误: ${e.message}")
            }
            */

            // 临时模拟成功 - 后端API完成后删除
            onSuccess()
        }.start()
    }

    /**
     * 标记消息为已读
     */
    fun markAsRead(
        messageId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        Thread {
            Thread.sleep(200)
            onSuccess()
        }.start()
    }

    /**
     * 清除所有未读消息
     */
    fun clearAllUnread(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        Thread {
            Thread.sleep(200)
            onSuccess()
        }.start()
    }
}