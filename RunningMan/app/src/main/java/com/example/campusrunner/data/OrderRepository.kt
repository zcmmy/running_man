package com.example.campusrunner.data

import com.example.campusrunner.model.Order
import com.example.campusrunner.model.OrderHistoryStatus
import com.example.campusrunner.model.OrderListResponse
import com.example.campusrunner.model.OrderStats
import com.example.campusrunner.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date

/**
 * 订单数据仓库
 * 管理订单数据的获取和状态
 */
class OrderRepository {

    private val apiService = RetrofitClient.apiService // 使用现有的 ApiService

    private val _publishedOrders = MutableStateFlow<List<Order>>(emptyList())
    val publishedOrders: StateFlow<List<Order>> = _publishedOrders.asStateFlow()

    private val _acceptedOrders = MutableStateFlow<List<Order>>(emptyList())
    val acceptedOrders: StateFlow<List<Order>> = _acceptedOrders.asStateFlow()

    private val _orderStats = MutableStateFlow<OrderStats?>(null)
    val orderStats: StateFlow<OrderStats?> = _orderStats.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * 获取发布的订单列表
     */
    suspend fun fetchPublishedOrders(status: OrderHistoryStatus? = null) {
        _isLoading.value = true
        _error.value = null

        try {
            // TODO: 当后端API可用时，取消注释以下代码并移除模拟数据
            /*
            val response = apiService.getPublishedOrders(
                page = 1,
                pageSize = 20,
                status = status?.name
            )
            if (response.isSuccessful) {
                _publishedOrders.value = response.body()?.orders ?: emptyList()
            } else {
                _error.value = "获取发布订单失败: ${response.message()}"
            }
            */

            // 模拟数据 - 后端API完成后删除
            _publishedOrders.value = getMockPublishedOrders(status)
        } catch (e: Exception) {
            _error.value = "网络错误: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * 获取接单的订单列表
     */
    suspend fun fetchAcceptedOrders(status: OrderHistoryStatus? = null) {
        _isLoading.value = true
        _error.value = null

        try {
            // TODO: 当后端API可用时，取消注释以下代码并移除模拟数据
            /*
            val response = apiService.getAcceptedOrders(
                page = 1,
                pageSize = 20,
                status = status?.name
            )
            if (response.isSuccessful) {
                _acceptedOrders.value = response.body()?.orders ?: emptyList()
            } else {
                _error.value = "获取接单订单失败: ${response.message()}"
            }
            */

            // 模拟数据 - 后端API完成后删除
            _acceptedOrders.value = getMockAcceptedOrders(status)
        } catch (e: Exception) {
            _error.value = "网络错误: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * 获取订单统计信息
     */
    suspend fun fetchOrderStats() {
        try {
            // TODO: 当后端API可用时，取消注释以下代码并移除模拟数据
            /*
            val response = apiService.getOrderStats()
            if (response.isSuccessful) {
                _orderStats.value = response.body()
            }
            */

            // 模拟数据 - 后端API完成后删除
            _orderStats.value = OrderStats(
                totalPublished = 15,
                totalAccepted = 8,
                totalCompleted = 6,
                totalIncome = 120.0
            )
        } catch (e: Exception) {
            // 忽略错误，统计信息不是关键数据
        }
    }

    /**
     * 接单操作
     */
    suspend fun acceptOrder(orderId: String): Boolean {
        try {
            // TODO: 当后端API可用时，取消注释以下代码
            /*
            val response = apiService.acceptOrder(orderId)
            return response.isSuccessful && response.body()?.code == 200
            */

            // 模拟成功 - 后端API完成后删除
            return true
        } catch (e: Exception) {
            _error.value = "接单失败: ${e.message}"
            return false
        }
    }

    /**
     * 完成订单操作
     */
    suspend fun completeOrder(orderId: String): Boolean {
        try {
            // TODO: 当后端API可用时，取消注释以下代码
            /*
            val response = apiService.completeOrder(orderId)
            return response.isSuccessful && response.body()?.code == 200
            */

            // 模拟成功 - 后端API完成后删除
            return true
        } catch (e: Exception) {
            _error.value = "完成订单失败: ${e.message}"
            return false
        }
    }

    /**
     * 取消订单操作
     */
    suspend fun cancelOrder(orderId: String): Boolean {
        try {
            // TODO: 当后端API可用时，取消注释以下代码
            /*
            val response = apiService.cancelOrder(orderId)
            return response.isSuccessful && response.body()?.code == 200
            */

            // 模拟成功 - 后端API完成后删除
            return true
        } catch (e: Exception) {
            _error.value = "取消订单失败: ${e.message}"
            return false
        }
    }

    /**
     * 清除错误信息
     */
    fun clearError() {
        _error.value = null
    }

    // 模拟数据生成 - 后端API完成后删除
    private fun getMockPublishedOrders(status: OrderHistoryStatus? = null): List<Order> {
        val allOrders = listOf(
            Order(
                id = "1",
                title = "麦当劳大套餐",
                description = "校门口取餐，送至图书馆三楼 A301",
                price = 15.0,
                type = "外卖",
                status = OrderHistoryStatus.COMPLETED,
                createdAt = Date(System.currentTimeMillis() - 86400000 * 2),
                updatedAt = Date(System.currentTimeMillis() - 86400000),
                publisherId = "user1",
                publisherName = "小明",
                runnerId = "runner1",
                runnerName = "张同学",
                fromLocation = "校门口",
                toLocation = "图书馆三楼 A301",
                distance = 2.0,
                estimatedTime = 15
            ),
            Order(
                id = "2",
                title = "打印 20 页文档",
                description = "打印店取 20 页文件，送至教学楼 502",
                price = 8.0,
                type = "打印",
                status = OrderHistoryStatus.IN_PROGRESS,
                createdAt = Date(System.currentTimeMillis() - 3600000),
                updatedAt = Date(System.currentTimeMillis() - 1800000),
                publisherId = "user1",
                publisherName = "小明",
                runnerId = "runner2",
                runnerName = "李同学",
                fromLocation = "打印店",
                toLocation = "教学楼 502",
                distance = 1.0,
                estimatedTime = 10
            ),
            Order(
                id = "3",
                title = "取快递",
                description = "韵达快递，收件人：小明",
                price = 5.0,
                type = "快递",
                status = OrderHistoryStatus.PENDING,
                createdAt = Date(System.currentTimeMillis() - 1800000),
                updatedAt = Date(System.currentTimeMillis() - 1800000),
                publisherId = "user1",
                publisherName = "小明",
                runnerId = null,
                runnerName = null,
                fromLocation = "快递点",
                toLocation = "宿舍楼",
                distance = 0.5,
                estimatedTime = 5
            )
        )

        return if (status != null) {
            allOrders.filter { it.status == status }
        } else {
            allOrders
        }
    }

    // 模拟数据生成 - 后端API完成后删除
    private fun getMockAcceptedOrders(status: OrderHistoryStatus? = null): List<Order> {
        val allOrders = listOf(
            Order(
                id = "4",
                title = "买感冒药",
                description = "校医院取感冒药，送至宿舍",
                price = 25.0,
                type = "药品",
                status = OrderHistoryStatus.COMPLETED,
                createdAt = Date(System.currentTimeMillis() - 86400000 * 3),
                updatedAt = Date(System.currentTimeMillis() - 86400000 * 2),
                publisherId = "user2",
                publisherName = "小红",
                runnerId = "user1",
                runnerName = "小明",
                fromLocation = "校医院",
                toLocation = "宿舍楼",
                distance = 1.5,
                estimatedTime = 10
            ),
            Order(
                id = "5",
                title = "买冰可乐",
                description = "超市买一瓶冰可乐，送到操场",
                price = 4.0,
                type = "饮料",
                status = OrderHistoryStatus.IN_PROGRESS,
                createdAt = Date(System.currentTimeMillis() - 3600000),
                updatedAt = Date(System.currentTimeMillis() - 1800000),
                publisherId = "user3",
                publisherName = "小刚",
                runnerId = "user1",
                runnerName = "小明",
                fromLocation = "超市",
                toLocation = "操场",
                distance = 1.2,
                estimatedTime = 8
            )
        )

        return if (status != null) {
            allOrders.filter { it.status == status }
        } else {
            allOrders
        }
    }
}