package com.example.campusrunner.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.campusrunner.model.ChatMessage
import com.example.campusrunner.viewmodels.ChatViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavController,
    orderId: String, // orderId 仍然由导航传入
    viewModel: ChatViewModel = viewModel() // 自动获取 ViewModel 实例
) {
    // 从 ViewModel 订阅状态
    val messages by viewModel.messages.collectAsState()
    val messageText by viewModel.messageText.collectAsState()
    // val error by viewModel.error.collectAsState() // 可以用来显示错误提示

    val listState = rememberLazyListState()

    // 当消息列表更新时，自动滚动到底部
    LaunchedEffect(messages) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = {
                    Text(
                        text = "与跑腿员聊天",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 消息列表
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                state = listState
            ) {
                // 使用来自 ViewModel 的真实消息列表
                items(messages) { message ->
                    ChatBubble(
                        message = message,
                        // 使用 ViewModel 判断是否是当前用户发送的
                        isSent = viewModel.isMessageSentByCurrentUser(message)
                    )
                }
            }

            // 输入区域
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = messageText, // 绑定到 ViewModel
                    onValueChange = { viewModel.onMessageChanged(it) }, // 更新 ViewModel
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("输入消息...") },
                    shape = RoundedCornerShape(24.dp)
                )

                IconButton(
                    onClick = {
                        // 调用 ViewModel 的发送方法
                        viewModel.sendMessage()
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .padding(start = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Send,
                        contentDescription = "发送",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubble(
    message: ChatMessage,
    isSent: Boolean
) {
    // [Alignment] 气泡的对齐方式：
    // isSent 为 true (用户) -> Alignment.CenterEnd (靠右)
    // isSent 为 false (对方) -> Alignment.CenterStart (靠左)
    val alignment = if (isSent) Alignment.CenterEnd else Alignment.CenterStart

    // [Color] 气泡的颜色：
    // isSent 为 true (用户) -> 浅蓝色 (0xFFD0E6FF)
    // isSent 为 false (对方) -> 浅灰色 (0xFFF0F0F0)
    val bubbleColor = if (isSent) Color(0xFFD0E6FF) else Color(0xFFF0F0F0)

    // [Text Color] 文字的颜色：
    // isSent 为 true (用户) -> 黑色 (在浅蓝色背景上)
    // isSent 为 false (对方) -> 黑色 (在浅灰色背景上)
    val textColor = Color.Black

    // [Timestamp Color] 时间戳的颜色
    val timestampColor = Color.Gray

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        contentAlignment = alignment // <-- 使用定义好的对齐
    ) {
        Card(
            modifier = Modifier.padding(4.dp),
            shape = RoundedCornerShape(16.dp),
            colors = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = bubbleColor // <-- 使用定义好的气泡颜色
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                // [!! 已移除 !!]
                // 不再显示对方的 senderName

                // 消息内容
                Text(
                    // [FIX]: 使用 Elvis 运算符 (?:) 提供默认值，防止 null
                    text = message.content ?: "",
                    color = textColor // <-- 使用定义好的文字颜色
                )

                // 时间戳
                Text(
                    // [FIX]: formatTimestamp 函数现在可以安全处理 null
                    text = formatTimestamp(message.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = timestampColor.copy(alpha = 0.7f), // <-- 使用定义好的时间戳颜色
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

/**
 * 格式化时间戳 (Date) 为 "HH:mm" 格式
 * (假设 ChatMessage.timestamp 是 java.util.Date)
 *
 * [FIX]: 接受一个可空的 Date? 类型
 */
@Composable
private fun formatTimestamp(timestamp: Date?): String {
    return remember(timestamp) {
        if (timestamp == null) {
            "--:--" // [FIX]: 如果 timestamp 为 null，返回一个安全的占位符
        } else {
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(timestamp)
        }
    }
}

