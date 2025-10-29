package com.example.campusrunner.data

import com.example.campusrunner.model.LiveOrder
import com.example.campusrunner.model.Location
import com.example.campusrunner.model.OrderMessage
import com.example.campusrunner.model.OrderMessageType
import com.example.campusrunner.model.OrderStatus
import com.example.campusrunner.network.ApiService
import com.example.campusrunner.network.MessageRequest
import com.example.campusrunner.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date

/**
 * 实时订单数据仓库 - (已更新为使用协程和真实API)
 */
object LiveOrderRepository {

    // 移除了 mockLiveOrder 模拟数据

    // 注入 ApiService
    private val apiService: ApiService = RetrofitClient.apiService

    // 监听器列表，用于模拟的WebSocket更新
    private val listeners = mutableListOf<(LiveOrder?) -> Unit>()

    /**
     * 功能：获取当前用户的实时订单 (已更新为 suspend 函数)
     * 后端API: GET /api/orders/current
     */
    suspend fun getCurrentLiveOrder(): LiveOrder? {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getCurrentOrders()
                if (response.isSuccessful) {
                    // API返回一个列表，我们取第一个作为当前的活跃订单
                    // 你可能需要更复杂的逻辑来处理多个活跃订单
                    response.body()?.firstOrNull()
                } else {
                    throw Exception("获取实时订单失败: ${response.code()}")
                }
            } catch (e: Exception) {
                throw Exception("网络错误 (getCurrentLiveOrder): ${e.message}")
            }
        }
    }

    /**
     * 功能：订阅实时订单更新（WebSocket）
     * (保留模拟实现，真实实现需要WebSocket客户端)
     */
    fun subscribeToOrderUpdates(
        orderId: String,
        onUpdate: (LiveOrder) -> Unit
    ): () -> Unit {
        // TODO: 将此实现替换为真实的 WebSocket 客户端连接
        // 模拟实时更新
        val updateThread = Thread {
            try {
                while (!Thread.interrupted()) {
                    Thread.sleep(30000) // 每30秒模拟一次更新
                    // 在真实应用中, WebSocket 消息会触发 onUpdate(newOrder)
                    // 这里我们无法模拟，因为 mockLiveOrder 已被移除
                    // 你需要通过WebSocket接收更新并调用 onUpdate
                }
            } catch (e: InterruptedException) {
                // 线程被中断
            }
        }
        updateThread.start()

        // 返回取消订阅的函数
        return {
            updateThread.interrupt()
        }
    }

    /**
     * 获取订单地图数据 (已更新为 suspend 函数)
     * 后端API: GET /api/orders/{orderId}/tracking
     */
    suspend fun getOrderMapData(orderId: String): LiveOrder {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getOrderTracking(orderId)
                if (response.isSuccessful && response.body() != null) {
                    response.body()!!
                } else {
                    throw Exception("获取订单地图数据失败: ${response.code()}")
                }
            } catch (e: Exception) {
                throw Exception("网络错误 (getOrderMapData): ${e.message}")
            }
        }
    }

    /**
     * 发送消息给跑腿员 (已更新为 suspend 函数)
     * 后端API: POST /api/chats/{orderId}/messages
     */
    suspend fun sendMessageToRunner(orderId: String, message: String) {
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.sendMessage(orderId, MessageRequest(content = message))
                if (!response.isSuccessful || response.body()?.code != 200) {
                    throw Exception("发送消息失败: ${response.body()?.message ?: "未知错误"}")
                }
                // 成功
            } catch (e: Exception) {
                throw Exception("网络错误 (sendMessageToRunner): ${e.message}")
            }
        }
    }

    /**
     * 清除实时订单数据（用于订单完成或退出时）
     */
    fun clearLiveOrder() {
        // TODO: 后端接入后可能需要添加的清理逻辑
        // - 停止WebSocket连接
        // - 取消位置更新订阅
        listeners.clear()
    }

    /**
     * 完成订单 (已更新为 suspend 函数)
     * 后端API: POST /api/orders/{orderId}/complete
     */
    suspend fun completeOrder(orderId: String) {
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.completeOrder(orderId)
                if (!response.isSuccessful || response.body()?.code != 200) {
                    throw Exception("完成订单失败: ${response.body()?.message ?: "未知错误"}")
                }
                // 成功
            } catch (e: Exception) {
                throw Exception("网络错误 (completeOrder): ${e.message}")
            }
        }
    }
}
