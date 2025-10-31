package com.example.campusrunner.model

import java.util.Date

/**
 * 实时订单状态
 */
data class LiveOrder(
    val id: String,
    val orderId: String,
    val orderTitle: String,
    val reward: Double,
    val runnerId: String,
    val runnerName: String,
    val runnerAvatar: String? = null,
    val runnerPhone: String? = null,
    val status: TaskStatus,
    val estimatedArrival: Date? = null,
    val currentLocation: Location? = null,
    val pickupLocation: Location,
    val deliveryLocation: Location,
    val lastUpdated: Date,
    val messages: List<OrderMessage> = emptyList()
) {
    // 获取预计到达时间文本
    fun getEstimatedTimeText(): String {
        estimatedArrival?.let { arrivalTime ->
            val now = Date()
            val diff = arrivalTime.time - now.time
            val minutes = diff / (60 * 1000)

            return when {
                minutes <= 0 -> "已到达"
                minutes < 60 -> "约${minutes}分钟"
                else -> "约${minutes / 60}小时"
            }
        }
        return "时间待定"
    }

    // 获取最后更新时间文本
    fun getLastUpdatedText(): String {
        val now = Date()
        val diff = now.time - lastUpdated.time
        val minutes = diff / (60 * 1000)

        return when {
            minutes < 1 -> "刚刚更新"
            minutes < 60 -> "${minutes}分钟前更新"
            else -> "${minutes / 60}小时前更新"
        }
    }
}

/**
 * 订单状态（更细粒度的状态）
 */

/**
 * 地理位置信息
 */
data class Location(
    val latitude: Double,
    val longitude: Double,
    val address: String? = null,
    val timestamp: Date = Date()
) {
    // 计算两个位置之间的距离（米）
    fun distanceTo(other: Location): Double {
        val earthRadius = 6371000.0 // 地球半径（米）

        val dLat = Math.toRadians(other.latitude - latitude)
        val dLon = Math.toRadians(other.longitude - longitude)

        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(latitude)) * Math.cos(Math.toRadians(other.latitude)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)

        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return earthRadius * c
    }
}

/**
 * 订单消息更新
 */
data class OrderMessage(
    val id: String,
    val orderId: String,
    val type: OrderMessageType,
    val content: String,
    val timestamp: Date,
    val location: Location? = null
)

/**
 * 订单消息类型
 */
enum class OrderMessageType {
    STATUS_UPDATE,  // 状态更新
    LOCATION_UPDATE, // 位置更新
    RUNNER_MESSAGE, // 跑腿员消息
    SYSTEM_MESSAGE  // 系统消息
}