package com.example.campusrunner.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusrunner.data.OrderRepository
import com.example.campusrunner.model.Order
import com.example.campusrunner.model.OrderHistoryStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 订单历史页面的 ViewModel
 * 负责处理订单历史相关的业务逻辑和状态管理
 */
class OrderHistoryViewModel : ViewModel() {

    // (MODIFIED) 实例化正确的 OrderRepository
    // 注意：OrderRepository 是一个 class，所以我们在这里创建它的实例
    private val repository = OrderRepository()

    // (MODIFIED) 从 Repository 暴露 StateFlow
    // UI (Screen) 将会监听这些状态
    val publishedOrders: StateFlow<List<Order>> = repository.publishedOrders
    val acceptedOrders: StateFlow<List<Order>> = repository.acceptedOrders
    val isLoading: StateFlow<Boolean> = repository.isLoading
    val error: StateFlow<String?> = repository.error

    /**
     * 获取用户发布的订单列表
     */
    fun fetchPublishedOrders(status: OrderHistoryStatus? = null) {
        viewModelScope.launch {
            repository.fetchPublishedOrders(status)
        }
    }

    /**
     * 获取用户接单的订单列表
     */
    fun fetchAcceptedOrders(status: OrderHistoryStatus? = null) {
        viewModelScope.launch {
            repository.fetchAcceptedOrders(status)
        }
    }
}
