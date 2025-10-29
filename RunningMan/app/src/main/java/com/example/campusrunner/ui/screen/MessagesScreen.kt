package com.example.campusrunner.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.campusrunner.ui.components.LiveOrderCard // 确保 LiveOrderCard 在这个包名下
import com.example.campusrunner.viewmodels.MessagesViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(navController: NavController? = null) {
    val viewModel: MessagesViewModel = viewModel()
    val chatSessions = viewModel.chatSessionsState.value
    val systemMessages = viewModel.systemMessagesState.value
    val liveOrder = viewModel.liveOrderState.value
    val isLoading = viewModel.loadingState.value
    val error = viewModel.errorState.value
    val selectedTab = viewModel.selectedTab.value

    // *** 修复点 1：从 ViewModel 获取 currentUserId ***
    val currentUserId = viewModel.currentUserId.value

    // Snackbar状态管理
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // 加载数据
    LaunchedEffect(Unit) {
        viewModel.loadMessages()
    }

    // 处理错误状态
    LaunchedEffect(error) {
        error?.let {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(it)
                viewModel.clearError()
            }
        }
    }

    Scaffold(
        topBar = {
            Column {
                Text(
                    text = "💬 消息",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )

                // 标签选择器
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    SegmentedButton(
                        selected = selectedTab == 0,
                        onClick = { viewModel.selectTab(0) },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.Chat,
                                contentDescription = "聊天"
                            )
                        }
                    ) {
                        Text("聊天")
                    }
                    SegmentedButton(
                        selected = selectedTab == 1,
                        onClick = { viewModel.selectTab(1) },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.Notifications,
                                contentDescription = "通知"
                            )
                        }
                    ) {
                        Text("通知")
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                // 修改加载逻辑：仅在首次加载时显示全屏加载
                isLoading && (liveOrder == null && chatSessions.isEmpty() && systemMessages.isEmpty()) -> {
                    // 加载中状态
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("加载消息中...")
                    }
                }
                error != null && !isLoading -> {
                    // 错误状态
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        androidx.compose.material3.Button(
                            onClick = { viewModel.loadMessages() }
                        ) {
                            Text("重新加载")
                        }
                    }
                }
                else -> {
                    // 正常显示内容
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // 实时订单卡片（只在有进行中订单时显示）
                        liveOrder?.let { order ->
                            if (order.status != com.example.campusrunner.model.OrderStatus.COMPLETED &&
                                order.status != com.example.campusrunner.model.OrderStatus.CANCELLED) {

                                // *** 修复点 2：更新 LiveOrderCard 调用 ***
                                LiveOrderCard(
                                    liveOrder = order,
                                    navController = navController,
                                    onSendMessage = { message ->
                                        viewModel.sendMessageToRunner(message)
                                    },
                                    // *** 传入新参数 ***
                                    currentUserId = currentUserId,
                                    onCompleteOrder = {
                                        viewModel.completeLiveOrder()
                                    }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }

                        // 消息列表
                        when (selectedTab) {
                            0 -> ChatSessionsList(
                                sessions = chatSessions,
                                onSessionClick = { session ->
                                    navController?.navigate("chat/${session.orderId}")
                                }
                            )
                            1 -> SystemMessagesList(
                                messages = systemMessages,
                                onMessageClick = { message ->
                                    viewModel.markMessageAsRead(message.id)
                                    // 如果是订单相关消息，可以跳转到订单详情
                                    message.orderId?.let { orderId ->
                                        navController?.navigate("detail/$orderId")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// (为了完整性，我从你上传的文件中复制了以下 Composable，并添加了 getTimeText 和 getTypeIcon 的简单实现)

// -----------------------------------------------------------------
// 以下是 MessagesScreen.kt 中的其余 Composable
// (为解决编译问题，我添加了 getTimeText 和 getTypeIcon 的辅助函数)
// -----------------------------------------------------------------

/**
 * 辅助函数：格式化日期
 */
private fun Date.getTimeText(): String {
    val now = Date()
    val diff = now.time - this.time
    val minutes = diff / (60 * 1000)
    val hours = minutes / 60
    val days = hours / 24

    return when {
        minutes < 1 -> "刚刚"
        minutes < 60 -> "${minutes}分钟前"
        hours < 24 -> "${hours}小时前"
        days < 2 -> "昨天"
        else -> SimpleDateFormat("MM-dd", Locale.getDefault()).format(this)
    }
}

/**
 * 辅助函数：获取消息图标
 */
private fun com.example.campusrunner.model.Message.getTypeIcon(): String {
    return when (this.type) {
        com.example.campusrunner.model.MessageType.ORDER_UPDATE -> "📦"
        com.example.campusrunner.model.MessageType.PROMOTION -> "🎉"
        com.example.campusrunner.model.MessageType.SYSTEM -> "⚙️"
        com.example.campusrunner.model.MessageType.CHAT -> "💬"
        else -> "💡"
    }
}


@Composable
fun ChatSessionsList(
    sessions: List<com.example.campusrunner.model.ChatSession>,
    onSessionClick: (com.example.campusrunner.model.ChatSession) -> Unit
) {
    if (sessions.isEmpty()) {
        EmptyState(
            title = "暂无聊天",
            message = "当您有进行中的订单时，可以在这里与跑腿员沟通"
        )
    } else {
        LazyColumn {
            items(sessions, key = { it.id }) { session ->
                ChatSessionItem(
                    session = session,
                    onClick = { onSessionClick(session) }
                )
            }
        }
    }
}

@Composable
fun ChatSessionItem(
    session: com.example.campusrunner.model.ChatSession,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 头像
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = session.participantName.first().toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 会话信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = session.participantName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = session.lastMessageTime.getTimeText(), // 使用辅助函数
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = session.orderTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = session.lastMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            // 未读消息计数
            if (session.unreadCount > 0) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = session.unreadCount.toString(),
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun SystemMessagesList(
    messages: List<com.example.campusrunner.model.Message>,
    onMessageClick: (com.example.campusrunner.model.Message) -> Unit
) {
    if (messages.isEmpty()) {
        EmptyState(
            title = "暂无通知",
            message = "系统通知和订单更新将在这里显示"
        )
    } else {
        LazyColumn {
            items(messages, key = { it.id }) { message ->
                SystemMessageItem(
                    message = message,
                    onClick = { onMessageClick(message) }
                )
            }
        }
    }
}

@Composable
fun SystemMessageItem(
    message: com.example.campusrunner.model.Message,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 消息类型图标
            Text(
                text = message.getTypeIcon(), // 使用辅助函数
                modifier = Modifier.padding(end = 12.dp),
                fontSize = MaterialTheme.typography.headlineMedium.fontSize
            )

            // 消息内容
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = message.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = if (!message.isRead) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    Text(
                        text = message.createdAt.getTimeText(), // 使用辅助函数
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 未读标识
            if (!message.isRead) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

@Composable
fun EmptyState(
    title: String,
    message: String
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

