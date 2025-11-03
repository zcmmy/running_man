package com.example.campusrunner.viewmodels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusrunner.data.LiveOrderRepository
import com.example.campusrunner.data.MessageRepository
import com.example.campusrunner.data.UserRepository
import com.example.campusrunner.model.ChatSession
import com.example.campusrunner.model.LiveOrder
import com.example.campusrunner.model.Message
import kotlinx.coroutines.launch

/**
 * 消息页面ViewModel
 * (已更新为使用协程并添加完成订单逻辑)
 */
class MessagesViewModel : ViewModel() {

    // 聊天会话状态
    private val _chatSessionsState = mutableStateOf<List<ChatSession>>(emptyList())
    val chatSessionsState: State<List<ChatSession>> = _chatSessionsState

    // 系统消息状态
    private val _systemMessagesState = mutableStateOf<List<Message>>(emptyList())
    val systemMessagesState: State<List<Message>> = _systemMessagesState

    // 实时订单状态
    private val _liveOrderState = mutableStateOf<LiveOrder?>(null)
    val liveOrderState: State<LiveOrder?> = _liveOrderState

    // 加载状态
    private val _loadingState = mutableStateOf(false)
    val loadingState: State<Boolean> = _loadingState

    // 错误状态
    private val _errorState = mutableStateOf<String?>(null)
    val errorState: State<String?> = _errorState

    // 当前选中的标签页
    private val _selectedTab = mutableStateOf(0)
    val selectedTab: State<Int> = _selectedTab

    // 新增: 当前用户ID
    private val _currentUserId = mutableStateOf<String?>(null)
    val currentUserId: State<String?> = _currentUserId

    // 取消订阅函数
    private var unsubscribeFromOrderUpdates: (() -> Unit)? = null

    /**
     * 加载消息数据
     * (已更新为使用 viewModelScope 和 suspend 函数)
     */
    fun loadMessages() {
        viewModelScope.launch {
            _loadingState.value = true
            _errorState.value = null

            try {
                // 1. 加载实时订单 (now suspend)
                loadLiveOrder() // 这将首先加载订单和用户ID

                // 2. 加载聊天会话 (调用 suspend 函数)
                val chats = MessageRepository.getChatSessions()
                _chatSessionsState.value = chats

                // 3. 加载系统消息 (调用 suspend 函数)
                val messages = MessageRepository.getSystemMessages()
                _systemMessagesState.value = messages

            } catch (e: Exception) {
                // 捕获 getChatSessions 或 getSystemMessages 抛出的异常
                val errorMessage = e.message ?: "加载消息失败"
                _errorState.value = errorMessage
                println("Error loading messages: $errorMessage")
            } finally {
                _loadingState.value = false
            }
        }
    }

    /**
     * 加载实时订单 (已更新为 suspend)
     */
    private suspend fun loadLiveOrder() {
        try {
            // 确保这里调用的是真实的 UserRepository
            _currentUserId.value = UserRepository.getCurrentUserId()

            // 调用 suspend 版的 getCurrentLiveOrder
            val liveOrder = LiveOrderRepository.getCurrentLiveOrder()
            _liveOrderState.value = liveOrder

            // 如果存在实时订单，订阅更新
            liveOrder?.let {
                unsubscribeFromOrderUpdates?.invoke() // 取消之前的订阅
                unsubscribeFromOrderUpdates = LiveOrderRepository.subscribeToOrderUpdates(
                    orderId = it.orderId,
                    onUpdate = { updatedOrder ->
                        // 确保在主线程更新状态
                        viewModelScope.launch {
                            _liveOrderState.value = updatedOrder
                        }
                    }
                )
            }
        } catch (e: Exception) {
            // 实时订单加载失败不应阻止其他消息加载
            println("加载实时订单失败: ${e.message}")
            _errorState.value = "加载实时订单失败: ${e.message}"
        }
    }

    /**
     * 切换标签页
     */
    fun selectTab(tabIndex: Int) {
        _selectedTab.value = tabIndex
    }

    // --- [!! 核心修复 !!] ---
    /**
     * (新函数) 立即在本地将聊天标记为已读
     * 这会为用户提供即时的UI反馈，防止竞争条件。
     * @param sessionId 要标记的 ChatSession 的 ID (可空)
     */
    fun markChatSessionAsReadLocally(sessionId: String?) {
        // [!! 修复 !!] 增加对 null 的检查
        if (sessionId == null) {
            println("markChatSessionAsReadLocally: sessionId 为 null，已忽略")
            return
        }

        val currentSessions = _chatSessionsState.value

        // 检查该会话是否真的存在且未读
        val sessionExists = currentSessions.any { it.id == sessionId && it.unreadCount > 0 }

        if (sessionExists) {
            // 创建一个新的列表，其中该会话的 unreadCount 已被设为 0
            _chatSessionsState.value = currentSessions.map { session ->
                if (session.id == sessionId) {
                    session.copy(unreadCount = 0)
                } else {
                    session
                }
            }
        }
        // 如果会话不存在或 unreadCount 已经为 0，则无需执行任何操作
    }
    // --- [!! 修复结束 !!] ---


    /**
     * 标记(系统)消息为已读
     */
    fun markMessageAsRead(messageId: String) {
        // (此函数不变，依赖 Repository 中的存根)
        MessageRepository.markAsRead(
            messageId = messageId,
            onSuccess = {
                _systemMessagesState.value = _systemMessagesState.value.map { message ->
                    if (message.id == messageId) message.copy(isRead = true) else message
                }
            },
            onError = { /* 忽略错误 */ }
        )
    }

    /**
     * 发送消息给跑腿员 (已更新为 suspend)
     */
    fun sendMessageToRunner(message: String) {
        _liveOrderState.value?.let { liveOrder ->
            viewModelScope.launch {
                try {
                    // 调用 suspend 版的 sendMessageToRunner
                    LiveOrderRepository.sendMessageToRunner(
                        orderId = liveOrder.orderId,
                        message = message
                    )
                    // 成功：在真实应用中，WebSocket会推送你自己的消息回来
                } catch (e: Exception) {
                    _errorState.value = "发送消息失败: ${e.message}"
                }
            }
        }
    }

    /**
     * 完成实时订单 (已更新)
     */
    fun completeLiveOrder() {
        _liveOrderState.value?.let { order ->
            if (_loadingState.value) return // 防止重复点击

            viewModelScope.launch {
                _loadingState.value = true
                try {
                    // 1. 标记订单完成 (你已有的逻辑)
                    LiveOrderRepository.completeOrder(order.orderId)

                    // 2. 订单完成后，为跑腿员增加余额
                    try {
                        // 假设 'order.reward' 字段已在 LiveOrder.kt 中添加
                        UserRepository.addBalance(order.runnerId, order.reward)
                    } catch (e: Exception) {
                        // 即使余额更新失败，订单也已完成。
                        println("警告: 订单已完成，但更新余额失败: ${e.message}")
                        _errorState.value = "订单已完成，但更新余额失败"
                    }

                    // 3. 清理本地状态
                    _liveOrderState.value = null // 清除本地的实时订单
                    unsubscribeFromOrderUpdates?.invoke() // 取消订阅
                    unsubscribeFromOrderUpdates = null

                    // 4. 重新加载所有数据以反映状态（聊天会话等）
                    loadMessages()

                } catch (e: Exception) {
                    _errorState.value = "完成订单失败: ${e.message}"
                } finally {
                    _loadingState.value = false
                }
            }
        }
    }


    /**
     * 清除错误状态
     */
    fun clearError() {
        _errorState.value = null
    }

    /**
     * 清理资源
     */
    override fun onCleared() {
        super.onCleared()
        unsubscribeFromOrderUpdates?.invoke() // 确保ViewModel销毁时取消订阅
    }
}

