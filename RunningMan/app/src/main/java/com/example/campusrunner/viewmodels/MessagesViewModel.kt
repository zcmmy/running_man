package com.example.campusrunner.viewmodels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusrunner.data.LiveOrderRepository
import com.example.campusrunner.data.MessageRepository
import com.example.campusrunner.model.ChatSession
import com.example.campusrunner.model.LiveOrder
import com.example.campusrunner.model.Message
import kotlinx.coroutines.launch

/**
 * 消息页面ViewModel
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

    // 取消订阅函数
    private var unsubscribeFromOrderUpdates: (() -> Unit)? = null

    /**
     * 加载消息数据
     */
    fun loadMessages() {
        _loadingState.value = true
        _errorState.value = null

        // 加载实时订单
        loadLiveOrder()

        // 加载聊天会话
        MessageRepository.getChatSessions(
            onSuccess = { chats ->
                _chatSessionsState.value = chats
                // 继续加载系统消息
                loadSystemMessages()
            },
            onError = { error ->
                _loadingState.value = false
                _errorState.value = error
            }
        )
    }

    /**
     * 加载实时订单
     */
    private fun loadLiveOrder() {
        LiveOrderRepository.getCurrentLiveOrder(
            onSuccess = { liveOrder ->
                _liveOrderState.value = liveOrder

                // 如果存在实时订单，订阅更新
                liveOrder?.let {
                    unsubscribeFromOrderUpdates = LiveOrderRepository.subscribeToOrderUpdates(
                        orderId = it.orderId,
                        onUpdate = { updatedOrder ->
                            _liveOrderState.value = updatedOrder
                        }
                    )
                }
            },
            onError = { error ->
                // 实时订单加载失败不影响其他功能
                println("加载实时订单失败: $error")
            }
        )
    }

    /**
     * 加载系统消息
     */
    private fun loadSystemMessages() {
        MessageRepository.getSystemMessages(
            onSuccess = { messages ->
                _systemMessagesState.value = messages
                _loadingState.value = false
            },
            onError = { error ->
                _loadingState.value = false
                _errorState.value = error
            }
        )
    }

    /**
     * 切换标签页
     */
    fun selectTab(tabIndex: Int) {
        _selectedTab.value = tabIndex
    }

    /**
     * 标记消息为已读
     */
    fun markMessageAsRead(messageId: String) {
        MessageRepository.markAsRead(
            messageId = messageId,
            onSuccess = {
                // 更新本地状态
                _systemMessagesState.value = _systemMessagesState.value.map { message ->
                    if (message.id == messageId) {
                        message.copy(isRead = true)
                    } else {
                        message
                    }
                }
            },
            onError = { /* 忽略错误，因为只是标记已读 */ }
        )
    }

    /**
     * 发送消息给跑腿员
     */
    fun sendMessageToRunner(message: String) {
        _liveOrderState.value?.let { liveOrder ->
            LiveOrderRepository.sendMessageToRunner(
                orderId = liveOrder.orderId,
                message = message,
                onSuccess = {
                    // 消息发送成功，实时订单会自动更新
                },
                onError = { error ->
                    // 处理发送失败
                    _errorState.value = "发送消息失败: $error"
                }
            )
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
        unsubscribeFromOrderUpdates?.invoke()
    }
}