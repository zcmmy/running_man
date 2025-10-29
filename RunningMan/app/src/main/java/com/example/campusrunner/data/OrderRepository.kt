package com.example.campusrunner.data

import com.example.campusrunner.model.Order
import com.example.campusrunner.model.OrderHistoryStatus
import com.example.campusrunner.model.OrderListResponse
import com.example.campusrunner.model.OrderStats
import com.example.campusrunner.network.RetrofitClient
// (MODIFIED) 导入 ApiService
import com.example.campusrunner.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date

/**
 * 订单数据仓库
 * 管理订单数据的获取和状态
 */
class OrderRepository {

    // (MODIFIED) 明确指定类型
    private val apiService: ApiService = RetrofitClient.apiService

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
     * 功能：获取用户发布的订单列表
     * (MODIFIED: 已接入后端)
     * 调用位置：OrderHistoryScreen, OrderHistoryViewModel
     */
    suspend fun fetchPublishedOrders(status: OrderHistoryStatus? = null) {
        _isLoading.value = true
        _error.value = null

        try {
            // (MODIFIED) 移除 // TODO 和 /* */
            val response = apiService.getPublishedOrders(
                page = 1,
                pageSize = 20,
                // (MODIFIED) 修正API调用：直接传递 status?.name 即可
                status = status?.name
            )
            if (response.isSuccessful) {
                _publishedOrders.value = response.body()?.orders ?: emptyList()
            } else {
                _error.value = "获取发布订单失败: ${response.message()}"
            }
            // (MODIFIED) 移除模拟数据
        } catch (e: Exception) {
            _error.value = "网络错误: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * 功能：获取用户接单的订单列表
     * (MODIFIED: 已接入后端)
     * 调用位置：OrderHistoryScreen, OrderHistoryViewModel
     */
    suspend fun fetchAcceptedOrders(status: OrderHistoryStatus? = null) {
        _isLoading.value = true
        _error.value = null

        try {
            // (MODIFIED) 移除 // TODO 和 /* */
            val response = apiService.getAcceptedOrders(
                page = 1,
                pageSize = 20,
                // (MODIFIED) 修正API调用：直接传递 status?.name 即可
                status = status?.name
            )
            if (response.isSuccessful) {
                _acceptedOrders.value = response.body()?.orders ?: emptyList()
            } else {
                _error.value = "获取接单订单失败: ${response.message()}"
            }
            // (MODIFIED) 移除模拟数据
        } catch (e: Exception) {
            _error.value = "网络错误: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * 功能：获取订单统计信息
     * (MODIFIED: 已接入后端)
     * 调用位置：ProfileScreen, 用户个人页面
     */
    suspend fun fetchOrderStats() {
        try {
            // (MODIFIED) 移除 // TODO 和 /* */
            val response = apiService.getOrderStats()
            if (response.isSuccessful) {
                _orderStats.value = response.body()
            }
            // (MODIFIED) 移除模拟数据
        } catch (e: Exception) {
            // 忽略错误，统计信息不是关键数据
        }
    }

    /**
     * 功能：接单操作
     * (MODIFIED: 已接入后端)
     * 调用位置：任务列表，用户点击接单时
     */
    suspend fun acceptOrder(orderId: String): Boolean {
        try {
            // (MODIFIED) 移除 // TODO 和 /* */
            val response = apiService.acceptOrder(orderId)
            return response.isSuccessful && response.body()?.code == 200
            // (MODIFIED) 移除模拟数据
        } catch (e: Exception) {
            _error.value = "接单失败: ${e.message}"
            return false
        }
    }

    /**
     * 完成订单操作
     * (MODIFIED: 已接入后端)
     * 调用位置：订单详情页面，跑腿员点击完成订单时
     */
    suspend fun completeOrder(orderId: String): Boolean {
        try {
            val response = apiService.completeOrder(orderId)
            if (response.isSuccessful && response.body()?.code == 200) {
                // 订单完成成功后，可以更新本地订单状态
                _acceptedOrders.value = _acceptedOrders.value.map { order ->
                    if (order.id == orderId) {
                        order.copy(status = OrderHistoryStatus.COMPLETED)
                    } else {
                        order
                    }
                }
                return true
            } else {
                _error.value = "完成订单失败: ${response.body()?.message ?: "未知错误"}"
                return false
            }
            // (MODIFIED) 移除模拟数据
        } catch (e: Exception) {
            _error.value = "完成订单失败: ${e.message}"
            return false
        }
    }

    /**
     * 取消订单操作
     * (MODIFIED: 已接入后端)
     * 调用位置：订单详情页面，用户点击取消订单时
     */
    suspend fun cancelOrder(orderId: String): Boolean {
        try {
            // (MODIFIED) 移除 // TODO 和 /* */
            val response = apiService.cancelOrder(orderId)
            if (response.isSuccessful && response.body()?.code == 200) {
                // 订单取消成功后，更新本地订单状态
                _publishedOrders.value = _publishedOrders.value.map { order ->
                    if (order.id == orderId) {
                        order.copy(status = OrderHistoryStatus.CANCELLED)
                    } else {
                        order
                    }
                }
                return true
            } else {
                _error.value = "取消订单失败: ${response.body()?.message ?: "未知错误"}"
                return false
            }
            // (MODIFIED) 移除模拟数据
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
}

