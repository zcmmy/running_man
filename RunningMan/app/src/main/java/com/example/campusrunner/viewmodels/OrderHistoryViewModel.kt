package com.example.campusrunner.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusrunner.data.OrderRepository
import com.example.campusrunner.model.OrderHistoryStatus // 修改导入
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OrderHistoryViewModel : ViewModel() {

    private val orderRepository = OrderRepository()

    val publishedOrders: StateFlow<List<com.example.campusrunner.model.Order>>
        get() = orderRepository.publishedOrders

    val acceptedOrders: StateFlow<List<com.example.campusrunner.model.Order>>
        get() = orderRepository.acceptedOrders

    val isLoading: StateFlow<Boolean>
        get() = orderRepository.isLoading

    val error: StateFlow<String?>
        get() = orderRepository.error

    init {
        // 初始化时加载数据
        fetchPublishedOrders()
        fetchAcceptedOrders()
    }

    fun fetchPublishedOrders(status: OrderHistoryStatus? = null) { // 修改参数类型
        viewModelScope.launch {
            orderRepository.fetchPublishedOrders(status)
        }
    }

    fun fetchAcceptedOrders(status: OrderHistoryStatus? = null) { // 修改参数类型
        viewModelScope.launch {
            orderRepository.fetchAcceptedOrders(status)
        }
    }

    fun clearError() {
        orderRepository.clearError()
    }
}