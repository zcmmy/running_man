package com.example.campusrunner.data

import android.util.Log
import com.example.campusrunner.model.ChatMessage
import com.example.campusrunner.model.ChatSession
import com.example.campusrunner.model.Message
import com.example.campusrunner.network.ApiService
import com.example.campusrunner.network.MessageRequest // 导入 MessageRequest
import com.example.campusrunner.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 消息数据仓库
 * (已更新为使用协程和真实API)
 */
object MessageRepository {

    // 移除了 mockChatSessions, mockSystemMessages, 和 mockChatMessages 模拟数据

    private val apiService: ApiService = RetrofitClient.apiService

    /**
     * 功能：获取聊天会话列表 (已更新为 suspend 函数)
     * 调用位置：MessagesViewModel
     */
    suspend fun getChatSessions(): List<ChatSession> {
        // 在 IO 线程上执行网络请求
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getChatSessions()
                Log.d("MessageReposity", "response: ${response.body()}")
                if (response.isSuccessful) {
                    response.body() ?: emptyList() // 成功则返回数据
                } else {
                    // 失败则抛出异常
                    throw Exception("获取聊天会话失败: ${response.code()}")
                }
            } catch (e: Exception) {
                // 捕获网络或其他异常
                throw Exception("网络错误 (getChatSessions): ${e.message}")
            }
        }
    }

    /**
     * 功能：获取聊天消息历史 (已更新为 suspend 函数)
     * 调用位置：ChatScreen (聊天页面)
     */
    suspend fun getChatMessages(orderId: String): List<ChatMessage> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getChatMessages(orderId)
                if (response.isSuccessful) {
                    response.body() ?: emptyList()
                } else {
                    throw Exception("获取聊天消息失败: ${response.code()}")
                }
            } catch (e: Exception) {
                throw Exception("网络错误 (getChatMessages): ${e.message}")
            }
        }
    }

    /**
     * 获取系统消息列表 (已更新为 suspend 函数)
     * 调用位置：MessagesViewModel
     */
    suspend fun getSystemMessages(): List<Message> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getSystemMessages()
                if (response.isSuccessful) {
                    response.body() ?: emptyList()
                } else {
                    throw Exception("获取系统消息失败: ${response.code()}")
                }
            } catch (e: Exception) {
                throw Exception("网络错误 (getSystemMessages): ${e.message}")
            }
        }
    }

    /**
     * 功能：发送聊天消息 (已更新为 suspend 函数)
     * 调用位置：ChatScreen (聊天页面)
     */
    suspend fun sendMessage(orderId: String, content: String) {
        withContext(Dispatchers.IO) {
            try {
                // 使用 ApiService.kt 中定义的 MessageRequest
                val response = apiService.sendMessage(orderId, MessageRequest(content))
                // 检查网络响应是否成功以及业务code是否为200
                if (!response.isSuccessful || response.body()?.code != 200) {
                    throw Exception("发送消息失败: ${response.body()?.message ?: "未知错误"}")
                }
                // 成功则无返回值 (Unit)
            } catch (e: Exception) {
                throw Exception("网络错误 (sendMessage): ${e.message}")
            }
        }
    }

    /**
     * 标记消息为已读
     * (保留存根，如需实现，请在 ApiService 中添加相应接口)
     */
    fun markAsRead(
        messageId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        // TODO: 添加真实API调用
        Thread {
            Thread.sleep(200)
            onSuccess()
        }.start()
    }

    /**
     * 清除所有未读消息
     * (保留存根，如需实现，请在 ApiService 中添加相应接口)
     */
    fun clearAllUnread(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        // TODO: 添加真实API调用
        Thread {
            Thread.sleep(200)
            onSuccess()
        }.start()
    }
}
