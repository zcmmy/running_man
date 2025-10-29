package com.example.campusrunner.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.campusrunner.model.LiveOrder
import com.example.campusrunner.model.OrderStatus

/**
 * 实时订单卡片 (新文件)
 *
 * @param liveOrder 实时订单数据
 * @param navController 导航控制器
 * @param onSendMessage 发送消息的回调
 * @param currentUserId 当前登录用户的ID (用于判断是否为跑腿员)
 * @param onCompleteOrder 完成订单的回调 (仅跑腿员可见)
 */
@Composable
fun LiveOrderCard(
    liveOrder: LiveOrder,
    navController: NavController?,
    onSendMessage: (String) -> Unit,
    currentUserId: String?,
    onCompleteOrder: () -> Unit
) {
    var messageText by remember { mutableStateOf("") }

    // *** 关键逻辑：判断当前用户是否为该订单的跑腿员 ***
    val isRunner = liveOrder.runnerId == currentUserId

    // *** 关键逻辑：判断订单是否处于可“完成”的状态 ***
    val canComplete = when (liveOrder.status) {
        OrderStatus.ACCEPTED,
        OrderStatus.PICKING_UP,
        OrderStatus.ON_THE_WAY,
        OrderStatus.ARRIVING -> true
        else -> false // WAITING_ACCEPT, COMPLETED, CANCELLED 不可完成
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 订单标题和状态
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = liveOrder.orderTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "状态: ${liveOrder.status}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            // 跑腿员信息
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 头像
                Text(
                    text = liveOrder.runnerName.firstOrNull()?.toString() ?: "R",
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .padding(8.dp),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = liveOrder.runnerName,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.weight(1f))
                // 联系跑腿员 (假设)
                IconButton(onClick = { /* TODO: 拨打电话 */ }) {
                    Icon(Icons.Default.Call, contentDescription = "联系跑腿员")
                }
                // 查看地图
                IconButton(onClick = { navController?.navigate("map/${liveOrder.orderId}") }) {
                    Icon(Icons.Default.Map, contentDescription = "查看地图")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 快捷聊天
            if (!isRunner) { // 只有顾客可以发送快捷消息
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    label = { Text("给跑腿员发消息...") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = {
                            if (messageText.isNotBlank()) {
                                onSendMessage(messageText)
                                messageText = ""
                            }
                        }) {
                            Icon(Icons.Default.Send, contentDescription = "发送")
                        }
                    }
                )
            }

            // *** 新增：完成订单按钮 ***
            // 只有当“当前用户是跑腿员” 且 “订单状态在进行中”时才显示
            if (isRunner && canComplete) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onCompleteOrder,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                    Text("我已送达 (完成订单)")
                }
            }
        }
    }
}
