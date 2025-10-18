package com.example.campusrunner.data

import com.example.campusrunner.model.LiveOrder
import com.example.campusrunner.model.Location
import com.example.campusrunner.model.OrderMessage
import com.example.campusrunner.model.OrderMessageType
import com.example.campusrunner.model.OrderStatus
import java.util.Date

/**
 * 实时订单数据仓库 - 处理实时订单跟踪和位置更新
 */
object LiveOrderRepository {

    // 模拟实时订单数据
    private var mockLiveOrder: LiveOrder? = LiveOrder(
        id = "live_1",
        orderId = "1",
        orderTitle = "麦当劳大套餐",
        runnerId = "runner1",
        runnerName = "张跑腿",
        runnerPhone = "138****1234",
        status = OrderStatus.ON_THE_WAY,
        estimatedArrival = Date(System.currentTimeMillis() + 15 * 60 * 1000), // 15分钟后
        currentLocation = Location(
            latitude = 30.5163,
            longitude = 114.4204,
            address = "华中科技大学南三门附近"
        ),
        pickupLocation = Location(
            latitude = 30.5150,
            longitude = 114.4180,
            address = "学校南门麦当劳"
        ),
        deliveryLocation = Location(
            latitude = 30.5175,
            longitude = 114.4220,
            address = "图书馆三楼 A301"
        ),
        routePolyline = listOf(
            Location(30.5150, 114.4180), // 起点
            Location(30.5155, 114.4190),
            Location(30.5160, 114.4200),
            Location(30.5165, 114.4210),
            Location(30.5170, 114.4215),
            Location(30.5175, 114.4220)  // 终点
        ),
        lastUpdated = Date(),
        messages = listOf(
            OrderMessage(
                id = "msg1",
                orderId = "1",
                type = OrderMessageType.STATUS_UPDATE,
                content = "跑腿员已接单",
                timestamp = Date(System.currentTimeMillis() - 25 * 60 * 1000)
            ),
            OrderMessage(
                id = "msg2",
                orderId = "1",
                type = OrderMessageType.LOCATION_UPDATE,
                content = "跑腿员已到达取货点",
                timestamp = Date(System.currentTimeMillis() - 20 * 60 * 1000)
            ),
            OrderMessage(
                id = "msg3",
                orderId = "1",
                type = OrderMessageType.STATUS_UPDATE,
                content = "开始配送",
                timestamp = Date(System.currentTimeMillis() - 10 * 60 * 1000)
            ),
            OrderMessage(
                id = "msg4",
                orderId = "1",
                type = OrderMessageType.RUNNER_MESSAGE,
                content = "我快到校门口了，大约5分钟",
                timestamp = Date(System.currentTimeMillis() - 5 * 60 * 1000)
            )
        )
    )

    // 监听器列表，用于实时更新
    private val listeners = mutableListOf<(LiveOrder?) -> Unit>()

    /**
     * 获取当前用户的实时订单
     * @param onSuccess 成功回调
     * @param onError 错误回调
     */
    fun getCurrentLiveOrder(
        onSuccess: (LiveOrder?) -> Unit,
        onError: (String) -> Unit
    ) {
        // 模拟网络请求
        Thread {
            Thread.sleep(300)

            // 在实际应用中，这里会调用真实的后端API
            // 例如：retrofitService.getCurrentLiveOrder().enqueue(...)

            onSuccess(mockLiveOrder)
        }.start()
    }

    /**
     * 订阅实时订单更新
     * @param orderId 订单ID
     * @param onUpdate 更新回调
     * @return 取消订阅的函数
     */
    fun subscribeToOrderUpdates(
        orderId: String,
        onUpdate: (LiveOrder) -> Unit
    ): () -> Unit {
        // 添加监听器
        listeners.add { liveOrder ->
            liveOrder?.let { onUpdate(it) }
        }

        // 模拟实时更新（在实际应用中，这里会使用WebSocket）
        val updateThread = Thread {
            while (true) {
                Thread.sleep(30000) // 每30秒更新一次

                // 模拟位置更新
                mockLiveOrder = mockLiveOrder?.copy(
                    currentLocation = Location(
                        latitude = 30.5163 + (Math.random() - 0.5) * 0.001,
                        longitude = 114.4204 + (Math.random() - 0.5) * 0.001,
                        address = "华中科技大学校内道路"
                    ),
                    lastUpdated = Date()
                )

                // 通知所有监听器
                listeners.forEach { it(mockLiveOrder) }

                // 如果订单已完成，停止更新
                if (mockLiveOrder?.status == OrderStatus.COMPLETED) {
                    break
                }
            }
        }
        updateThread.start()

        // 返回取消订阅的函数
        return {
            listeners.remove(onUpdate)
            updateThread.interrupt()
        }
    }

    /**
     * 获取订单地图数据
     * @param orderId 订单ID
     * @param onSuccess 成功回调
     * @param onError 错误回调
     */
    fun getOrderMapData(
        orderId: String,
        onSuccess: (LiveOrder) -> Unit,
        onError: (String) -> Unit
    ) {
        // 模拟网络请求
        Thread {
            Thread.sleep(400)

            mockLiveOrder?.let { onSuccess(it) } ?: onError("订单不存在")
        }.start()
    }

    /**
     * 发送消息给跑腿员
     * @param orderId 订单ID
     * @param message 消息内容
     * @param onSuccess 成功回调
     * @param onError 错误回调
     */
    fun sendMessageToRunner(
        orderId: String,
        message: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        // 模拟网络请求
        Thread {
            Thread.sleep(500)

            // 在实际应用中，这里会调用真实的后端API
            // 例如：retrofitService.sendMessage(orderId, message).enqueue(...)

            // 添加到消息列表
            val newMessage = OrderMessage(
                id = "msg_${System.currentTimeMillis()}",
                orderId = orderId,
                type = OrderMessageType.RUNNER_MESSAGE,
                content = "您：$message",
                timestamp = Date()
            )

            mockLiveOrder = mockLiveOrder?.copy(
                messages = mockLiveOrder?.messages?.plus(newMessage) ?: listOf(newMessage)
            )

            onSuccess()
        }.start()
    }

    /**
     * 清除实时订单数据（用于测试或订单完成时）
     */
    fun clearLiveOrder() {
        mockLiveOrder = null
        listeners.forEach { it(null) }
    }

    /**
     * 模拟完成订单
     */
    fun completeOrder() {
        mockLiveOrder = mockLiveOrder?.copy(
            status = OrderStatus.COMPLETED,
            currentLocation = mockLiveOrder?.deliveryLocation,
            lastUpdated = Date()
        )
        listeners.forEach { it(mockLiveOrder) }
    }
}