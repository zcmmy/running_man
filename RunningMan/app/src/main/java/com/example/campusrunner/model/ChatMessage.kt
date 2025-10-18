package com.example.campusrunner.model

import java.util.Date

/**
 * 聊天消息数据类
 * 用于表示用户与跑腿员之间的聊天消息
 */
data class ChatMessage(
    val id: String,
    val orderId: String,
    val senderId: String,
    val senderName: String,
    val senderAvatar: String? = null,
    val content: String,
    val messageType: ChatMessageType = ChatMessageType.TEXT,
    val timestamp: Date,
    val isRead: Boolean = false,
    val extraData: Map<String, String> = emptyMap() // 扩展数据，可用于存储图片URL等
) {
    // 获取时间显示
    fun getTimeText(): String {
        val now = Date()
        val diff = now.time - timestamp.time
        val minutes = diff / (60 * 1000)

        return when {
            minutes < 1 -> "刚刚"
            minutes < 60 -> "${minutes}分钟前"
            minutes < 1440 -> "${minutes / 60}小时前"
            else -> "${minutes / 1440}天前"
        }
    }

    // 判断消息是否由当前用户发送
    fun isSentByCurrentUser(currentUserId: String): Boolean {
        return senderId == currentUserId
    }
}

/**
 * 聊天消息类型枚举
 */
enum class ChatMessageType {
    TEXT,       // 文本消息
    IMAGE,      // 图片消息
    LOCATION,   // 位置消息
    SYSTEM      // 系统消息（如订单状态更新）
}