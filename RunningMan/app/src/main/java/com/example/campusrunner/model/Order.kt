package com.example.campusrunner.model

import java.util.Date

/**
 * 订单状态枚举（用于订单历史）
 */

/**
 * 订单数据模型
 */
data class Order(
    val id: String,
    val title: String,
    val description: String,
    val price: Double,
    val type: String, // 订单类型：外卖、快递、打印等
    val status: TaskStatus, // 修改为 OrderHistoryStatus
    val createdAt: Date,
    val updatedAt: Date,
    val publisherId: String, // 发布者ID
    val publisherName: String, // 发布者姓名
    val runnerId: String?, // 接单者ID（可为空）
    val runnerName: String?, // 接单者姓名（可为空）
    val fromLocation: String, // 起始位置
    val toLocation: String, // 目标位置
    val distance: Double, // 距离（公里）
    val estimatedTime: Int, // 预计时间（分钟）
    val images: List<String>? = null // 订单相关图片
)

/**
 * 订单列表响应
 */
data class OrderListResponse(
    val orders: List<Order>,
    val totalCount: Int,
    val page: Int,
    val pageSize: Int
)

/**
 * 订单统计信息
 */
data class OrderStats(
    val totalPublished: Int,
    val totalAccepted: Int,
    val totalCompleted: Int,
    val totalIncome: Double
)