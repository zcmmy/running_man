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
     * 功能：获取当前用户的实时订单
     * 后端接入步骤：
     * 1. 取消注释apiService.getCurrentOrders()调用
     * 2. 处理网络响应
     * 3. 移除模拟数据逻辑
     * 调用位置：实时订单跟踪页面
     */
    fun getCurrentLiveOrder(
        onSuccess: (LiveOrder?) -> Unit,
        onError: (String) -> Unit
    ) {
        // 模拟网络请求
        Thread {
            Thread.sleep(300)

            // 在实际应用中，这里会调用真实的后端API
            /*
            try {
                val response = apiService.getCurrentOrders().execute()
                if (response.isSuccessful) {
                    val orders = response.body() ?: emptyList()
                    onSuccess(orders.firstOrNull()) // 取第一个进行中的订单
                } else {
                    onError("获取实时订单失败: ${response.code()}")
                }
            } catch (e: Exception) {
                onError("网络错误: ${e.message}")
            }
            */

            onSuccess(mockLiveOrder)
        }.start()
    }

    /**
     * 订阅实时订单更新
     * @param orderId 订单ID
     * @param onUpdate 更新回调
     * @return 取消订阅的函数
     */
    /**
     * 功能：订阅实时订单更新（WebSocket）
     * 后端接入步骤：
     * 1. 实现WebSocket连接
     * 2. 处理实时位置更新消息
     * 3. 更新监听器
     * 调用位置：实时订单跟踪页面，订单进行中时
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
    /**
     * 获取订单地图数据
     * 后端接入步骤：
     * 1. 取消注释 apiService.getOrderTracking(orderId) 调用
     * 2. 处理网络响应，包括成功和错误情况
     * 3. 移除模拟数据逻辑
     * 调用位置：实时订单跟踪页面，地图显示时
     *
     * 后端API实现示例：
     * GET /api/orders/{orderId}/tracking
     * 请求示例：GET /api/orders/1/tracking
     * 请求头：需要Authorization token
     * 响应示例：
     * {
     *   "id": "live_1",
     *   "orderId": "1",
     *   "orderTitle": "麦当劳大套餐",
     *   "runnerId": "runner1",
     *   "runnerName": "张跑腿",
     *   "status": "ON_THE_WAY",
     *   "estimatedArrival": "2024-01-20T11:00:00Z",
     *   "currentLocation": {
     *     "latitude": 30.5163,
     *     "longitude": 114.4204,
     *     "address": "华中科技大学南三门附近"
     *   },
     *   "pickupLocation": {
     *     "latitude": 30.5150,
     *     "longitude": 114.4180,
     *     "address": "学校南门麦当劳"
     *   },
     *   "deliveryLocation": {
     *     "latitude": 30.5175,
     *     "longitude": 114.4220,
     *     "address": "图书馆三楼 A301"
     *   },
     *   "routePolyline": [
     *     {"latitude": 30.5150, "longitude": 114.4180},
     *     {"latitude": 30.5175, "longitude": 114.4220}
     *   ],
     *   "lastUpdated": "2024-01-20T10:45:00Z"
     * }
     *
     * 注意事项：
     * - 地图数据包括跑腿员当前位置、路线规划和预计到达时间
     * - 路线坐标点用于在地图上绘制配送路径
     * - 需要定期轮询或使用WebSocket获取实时位置更新
     */
    fun getOrderMapData(
        orderId: String,
        onSuccess: (LiveOrder) -> Unit,
        onError: (String) -> Unit
    ) {
        // 模拟网络请求
        Thread {
            Thread.sleep(400)

            // TODO: 当后端API准备好后，取消注释以下代码
            /*
            try {
                val response = apiService.getOrderTracking(orderId).execute()
                if (response.isSuccessful && response.body() != null) {
                    onSuccess(response.body()!!)
                } else {
                    onError("获取订单地图数据失败: ${response.code()}")
                }
            } catch (e: Exception) {
                onError("网络错误: ${e.message}")
            }
            */

            // 临时使用模拟数据 - 后端API完成后删除
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
    /**
     * 发送消息给跑腿员
     * 后端接入步骤：
     * 1. 实现真实的后端API调用发送消息
     * 2. 处理网络响应，包括成功和错误情况
     * 3. 移除模拟消息添加逻辑
     * 调用位置：实时订单跟踪页面，用户发送消息时
     *
     * 后端API实现示例：
     * POST /api/chats/{orderId}/messages
     * 请求示例：POST /api/chats/1/messages
     * 请求头：需要Authorization token
     * 请求体：
     * {
     *   "content": "请帮我买一瓶可乐",
     *   "type": "CHAT"
     * }
     * 响应示例：
     * {
     *   "code": 200,
     *   "message": "消息发送成功",
     *   "data": "消息ID：msg123"
     * }
     *
     * 注意事项：
     * - 消息发送后应该立即显示在聊天界面
     * - 需要处理消息发送失败的重试机制
     * - 消息应该实时推送给跑腿员（使用WebSocket）
     * - 消息内容需要做敏感词过滤和安全检查
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

            // TODO: 当后端API准备好后，取消注释以下代码
            /*
            try {
                val response = apiService.sendMessage(orderId, MessageRequest(content = message)).execute()
                if (response.isSuccessful && response.body()?.code == 200) {
                    // 消息发送成功后，可以更新本地消息列表
                    val newMessage = ChatMessage(
                        id = response.body()?.data ?: "msg_${System.currentTimeMillis()}",
                        orderId = orderId,
                        senderId = "current_user", // 当前用户ID
                        senderName = "您",
                        content = message,
                        messageType = ChatMessageType.TEXT,
                        timestamp = Date(),
                        isRead = false
                    )

                    // 更新本地消息列表
                    mockLiveOrder = mockLiveOrder?.copy(
                        messages = mockLiveOrder?.messages?.plus(newMessage) ?: listOf(newMessage)
                    )

                    onSuccess()
                } else {
                    onError("发送消息失败: ${response.body()?.message ?: "未知错误"}")
                }
            } catch (e: Exception) {
                onError("网络错误: ${e.message}")
            }
            */

            // 临时模拟成功 - 后端API完成后删除
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
     * 后端接入步骤：
     * 1. 订单完成后调用此函数清理本地缓存
     * 2. 通知所有监听器订单状态已结束
     * 3. 停止实时位置更新订阅
     * 调用位置：订单完成时，用户退出实时跟踪页面时
     *
     * 注意事项：
     * - 订单完成后必须调用此函数清理资源
     * - 停止所有相关的WebSocket连接和定时器
     * - 清理本地缓存的实时订单数据
     * - 通知所有UI组件更新状态
     */
    fun clearLiveOrder() {
        mockLiveOrder = null
        listeners.forEach { it(null) }

        // TODO: 后端接入后可能需要添加的清理逻辑
        // - 停止WebSocket连接
        // - 取消位置更新订阅
        // - 清理本地数据库缓存
    }

    /**
     * 模拟完成订单
     * 后端接入步骤：
     * 1. 替换为真实的订单完成API调用
     * 2. 处理订单完成后的状态更新
     * 3. 移除模拟数据更新逻辑
     * 调用位置：测试用途，实际应该使用OrderRepository.completeOrder()
     *
     * 真实实现应该：
     * 1. 调用 apiService.completeOrder(orderId)
     * 2. 处理网络响应
     * 3. 更新本地订单状态
     * 4. 通知所有监听器订单已完成
     * 5. 清理实时订单数据
     *
     * 注意事项：
     * - 此函数主要用于测试，实际订单完成应使用OrderRepository
     * - 订单完成后应该停止所有实时更新
     * - 需要更新订单历史记录
     * - 可能需要触发支付流程
     */
    fun completeOrder() {
        // TODO: 当后端API准备好后，应该调用真实的订单完成接口
        /*
        OrderRepository.completeOrder(mockLiveOrder?.orderId ?: "") { success ->
            if (success) {
                mockLiveOrder = mockLiveOrder?.copy(
                    status = OrderStatus.COMPLETED,
                    currentLocation = mockLiveOrder?.deliveryLocation,
                    lastUpdated = Date()
                )
                listeners.forEach { it(mockLiveOrder) }
            }
        }
        */

        // 临时模拟完成 - 后端API完成后删除
        mockLiveOrder = mockLiveOrder?.copy(
            status = OrderStatus.COMPLETED,
            currentLocation = mockLiveOrder?.deliveryLocation,
            lastUpdated = Date()
        )
        listeners.forEach { it(mockLiveOrder) }
    }
}