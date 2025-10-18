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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.campusrunner.model.ChatMessage
import com.example.campusrunner.model.ChatMessageType
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavController,
    orderId: String
) {
    var messageText by remember { mutableStateOf("") }

    // 模拟聊天消息 - 使用新的 ChatMessage 类
    val messages = listOf(
        ChatMessage(
            id = "1",
            orderId = orderId,
            senderId = "runner1",
            senderName = "张跑腿",
            content = "我快到校门口了。",
            messageType = ChatMessageType.TEXT,
            timestamp = Date(System.currentTimeMillis() - 10 * 60 * 1000)
        ),
        ChatMessage(
            id = "2",
            orderId = orderId,
            senderId = "current_user",
            senderName = "您",
            content = "好的，我下去取。",
            messageType = ChatMessageType.TEXT,
            timestamp = Date(System.currentTimeMillis() - 8 * 60 * 1000)
        ),
        ChatMessage(
            id = "3",
            orderId = orderId,
            senderId = "runner1",
            senderName = "张跑腿",
            content = "大约 5 分钟。",
            messageType = ChatMessageType.TEXT,
            timestamp = Date(System.currentTimeMillis() - 5 * 60 * 1000)
        ),
        ChatMessage(
            id = "4",
            orderId = orderId,
            senderId = "current_user",
            senderName = "您",
            content = "收到，谢谢！",
            messageType = ChatMessageType.TEXT,
            timestamp = Date(System.currentTimeMillis() - 3 * 60 * 1000)
        )
    )

    val listState = rememberLazyListState()

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
                items(messages) { message ->
                    ChatBubble(
                        message = message,
                        isSent = message.senderId == "current_user" // 假设当前用户ID是"current_user"
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
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("输入消息...") },
                    shape = RoundedCornerShape(24.dp)
                )

                IconButton(
                    onClick = {
                        // 发送消息逻辑
                        if (messageText.isNotBlank()) {
                            // 调用发送消息API
                            // MessageRepository.sendMessage(orderId, messageText) { ... }
                            messageText = ""
                        }
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        contentAlignment = if (isSent) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Card(
            modifier = Modifier.padding(4.dp),
            shape = RoundedCornerShape(16.dp),
            colors = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = if (isSent) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                // 显示发送者名称（只显示对方的名字）
                if (!isSent) {
                    Text(
                        text = message.senderName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                // 消息内容
                Text(
                    text = message.content,
                    color = if (isSent) {
                        Color.White
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )

                // 时间戳
                Text(
                    text = message.getTimeText(),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSent) {
                        Color.White.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}