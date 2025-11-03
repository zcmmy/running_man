package com.example.campusrunner.viewmodels

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusrunner.data.MessageRepository
import com.example.campusrunner.data.UserRepository // 1. 导入 UserRepository
import com.example.campusrunner.model.ChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

class ChatViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // 2. 从 SavedStateHandle 获取 orderId (这在 AppNavHost.kt 中已设置)
    private val orderId: String = checkNotNull(savedStateHandle["orderId"])

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _messageText = MutableStateFlow("")
    val messageText = _messageText.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private var pollingJob: Job? = null

    // 3. [!! 核心修复 !!]
    //    删除占位符，从 UserRepository (它是一个 object) 获取真实的用户 ID
    //    这依赖你提供的 UserRepository.kt 中的 getCurrentUserId()
    private val currentUserId: String? = UserRepository.getCurrentUserId()

    init {
        Log.d("ChatViewModel", "ViewModel created for orderId: $orderId. Current user ID: $currentUserId")
        if (currentUserId == null) {
            Log.e("ChatViewModel", "CRITICAL: currentUserId is null. Chat bubbles will be incorrect.")
            // 可以在此设置一个错误状态，通知 UI 用户未登录
            _error.value = "无法获取用户ID，请重新登录"
        }
        startPolling()
    }

    /**
     * 判断这条讯息是否由当前登录用户发送
     */
    fun isMessageSentByCurrentUser(message: ChatMessage): Boolean {
        // 4. [!! 核心修复 !!]
        //    现在这个比较可以正确工作了
        //    如果 currentUserId 为 null，它会返回 false (安全的)
        return message.senderId == currentUserId
    }

    /**
     * 更新输入框文字
     */
    fun onMessageChanged(newText: String) {
        _messageText.value = newText
    }

    /**
     * 发送消息
     */
    fun sendMessage() {
        val content = _messageText.value.trim()
        if (content.isBlank()) {
            return
        }

        viewModelScope.launch(Dispatchers.Main) {
            try {
                // 1. 立即清空输入框 (优化体验)
                _messageText.value = ""

                // 2. 调用 Repository
                MessageRepository.sendMessage(orderId, content)

                // 3. 立即刷新消息 (而不是等待下一次轮询)
                fetchMessages()

            } catch (e: Exception) {
                Log.e("ChatViewModel", "Failed to send message", e)
                _error.value = "发送失败: ${e.message}"
                // (可选) 恢复未发送的文字
                _messageText.value = content
            }
        }
    }

    /**
     * 开始轮询
     */
    private fun startPolling() {
        pollingJob?.cancel() // 确保旧的 job 已取消
        pollingJob = viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                try {
                    fetchMessages()
                } catch (e: Exception) {
                    Log.e("ChatViewModel", "Error polling messages", e)
                    // 可以在这里设置一个短暂的错误提示，但不要停止轮询
                    _error.value = "获取新消息失败"
                }
                delay(5000) // 5 秒钟轮询一次
            }
        }
    }

    /**
     * 获取消息的内部函数
     */
    private suspend fun fetchMessages() {
        val newMessages = MessageRepository.getChatMessages(orderId)
        // 仅在消息列表有变化时才更新 UI
        if (newMessages != _messages.value) {
            _messages.value = newMessages
        }
    }

    /**
     * ViewModel 销毁时停止轮询
     */
    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }
}

