package com.example.campusrunner.model

import java.util.Date

/**
 * 任务状态枚举
 */
enum class TaskStatus {
    PENDING,    // 待接单
    IN_PROGRESS, // 进行中
    COMPLETED,  // 已完成
    CANCELLED   // 已取消
}

/**
 * 任务类型枚举
 */
enum class TaskType {
    FOOD_DELIVERY,  // 外卖
    EXPRESS,        // 快递
    PRINT,          // 打印
    SHOPPING,       // 购物
    OTHER           // 其他
}

/**
 * 任务数据类
 */
data class Task(
    val id: String,
    val title: String,
    val description: String,
    val price: Double,
    val type: TaskType,
    val status: TaskStatus,
    val location: String,
    val destination: String,
    val distance: Double, // 距离，单位：km
    val publisherId: String,
    val publisherName: String,
    val publisherAvatar: String? = null,
    val publisherRating: Double = 0.0, // 发布者信用分
    val createdAt: Date,
    val acceptedAt: Date? = null,
    val completedAt: Date? = null,
    val runnerId: String? = null,
    val runnerName: String? = null,
    val contactPhone: String? = null, // 联系电话
    val estimatedTime: Int? = null, // 预计完成时间（分钟）
    val specialRequirements: String? = null // 特殊要求
) {
    // 格式化价格显示
    fun getFormattedPrice(): String = "¥${price.toInt()}"

    // 格式化距离显示
    fun getFormattedDistance(): String = "${distance}km"

    // 获取类型显示文本
    fun getTypeText(): String = when(type) {
        TaskType.FOOD_DELIVERY -> "外卖"
        TaskType.EXPRESS -> "快递"
        TaskType.PRINT -> "打印"
        TaskType.SHOPPING -> "购物"
        TaskType.OTHER -> "其他"
    }

    // 获取状态显示文本
    fun getStatusText(): String = when(status) {
        TaskStatus.PENDING -> "待接单"
        TaskStatus.IN_PROGRESS -> "进行中"
        TaskStatus.COMPLETED -> "已完成"
        TaskStatus.CANCELLED -> "已取消"
    }

    // 获取状态颜色
    fun getStatusColor(): androidx.compose.ui.graphics.Color = when(status) {
        TaskStatus.PENDING -> androidx.compose.ui.graphics.Color(0xFFFF6B35)
        TaskStatus.IN_PROGRESS -> androidx.compose.ui.graphics.Color(0xFF2196F3)
        TaskStatus.COMPLETED -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
        TaskStatus.CANCELLED -> androidx.compose.ui.graphics.Color(0xFF9E9E9E)
    }

    // 获取发布时间显示
    fun getCreatedTimeText(): String {
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

    // 获取预计完成时间显示
    fun getEstimatedTimeText(): String {
        return estimatedTime?.let { "预计${it}分钟完成" } ?: "时间待定"
    }
}