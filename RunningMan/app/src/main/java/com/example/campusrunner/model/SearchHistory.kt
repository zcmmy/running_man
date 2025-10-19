package com.example.campusrunner.model

import java.util.Date

/**
 * 搜索历史数据类
 */
data class SearchHistory(
    val id: String,
    val userId: String, // 关联用户ID
    val keyword: String,
    val searchCount: Int = 1, // 搜索次数
    val lastSearchedAt: Date,
    val createdAt: Date
) {
    /**
     * 获取时间显示文本
     */
    fun getTimeText(): String {
        val now = Date()
        val diff = now.time - lastSearchedAt.time
        val minutes = diff / (60 * 1000)

        return when {
            minutes < 1 -> "刚刚"
            minutes < 60 -> "${minutes}分钟前"
            minutes < 1440 -> "${minutes / 60}小时前"
            else -> "${minutes / 1440}天前"
        }
    }
}

/**
 * 搜索历史请求
 */
data class SearchHistoryRequest(
    val keyword: String,
    val userId: String
)

/**
 * 搜索历史响应
 */
data class SearchHistoryResponse(
    val histories: List<SearchHistory>,
    val total: Int
)