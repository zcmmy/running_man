package com.example.campusrunner.model

import java.util.Date

/**
 * 消息类型枚举
 */
enum class MessageType {
    CHAT,           // 聊天消息
    ORDER_UPDATE,   // 订单更新
    SYSTEM,         // 系统通知
    PROMOTION       // 推广消息
}

/**
 * 消息数据类
 */
data class Message(
    val id: String,
    val type: MessageType,
    val title: String,
    val content: String,
    val senderId: String,
    val senderName: String,
    val senderAvatar: String? = null,
    val orderId: String? = null,  // 关联的订单ID
    val isRead: Boolean = false,
    val createdAt: Date,
    val extraData: Map<String, String> = emptyMap() // 扩展数据
) {
    // 获取时间显示
    fun getTimeText(): String {
        val now = Date()
        val diff = now.time - createdAt.time
        val minutes = diff / (60 * 1000)

        return when {
            minutes < 1 -> "刚刚"
            minutes < 60 -> "${minutes}分钟前"
            minutes < 1440 -> "${minutes / 60}小时前"
            else -> "${minutes / 1440}天前"
        }
    }

    // 获取消息类型图标
    fun getTypeIcon(): String = when(type) {
        MessageType.CHAT -> "💬"
        MessageType.ORDER_UPDATE -> "📦"
        MessageType.SYSTEM -> "🔔"
        MessageType.PROMOTION -> "🎁"
    }
}

/**
 * 聊天会话数据类
 */
data class ChatSession(
    val id: String,
    val orderId: String,
    val orderTitle: String,
    val participantId: String,
    val participantName: String,
    val participantAvatar: String? = null,
    val lastMessage: String,
    val unreadCount: Int = 0,
    val lastMessageTime: Date,
    val orderStatus: TaskStatus
) {
    fun getTimeText(): String {
        val now = Date()
        val diff = now.time - lastMessageTime.time
        val minutes = diff / (60 * 1000)

        return when {
            minutes < 1 -> "刚刚"
            minutes < 60 -> "${minutes}分钟前"
            minutes < 1440 -> "${minutes / 60}小时前"
            else -> "${minutes / 1440}天前"
        }
    }
}