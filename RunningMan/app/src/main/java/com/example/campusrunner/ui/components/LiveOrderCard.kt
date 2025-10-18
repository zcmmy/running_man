package com.example.campusrunner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.campusrunner.model.LiveOrder
import com.example.campusrunner.model.OrderStatus

@Composable
fun LiveOrderCard(
    liveOrder: LiveOrder,
    navController: NavController? = null,
    onSendMessage: (String) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable {
                // 点击跳转到订单详情或订单跟踪页面
                navController?.navigate("detail/${liveOrder.orderId}")
            }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 头部：订单标题和状态
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📦 ${liveOrder.orderTitle}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = when (liveOrder.status) {
                        OrderStatus.WAITING_ACCEPT -> "待接单"
                        OrderStatus.ACCEPTED -> "已接单"
                        OrderStatus.PICKING_UP -> "取货中"
                        OrderStatus.ON_THE_WAY -> "配送中"
                        OrderStatus.ARRIVING -> "即将到达"
                        OrderStatus.COMPLETED -> "已完成"
                        OrderStatus.CANCELLED -> "已取消"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = when (liveOrder.status) {
                        OrderStatus.ON_THE_WAY, OrderStatus.ARRIVING -> MaterialTheme.colorScheme.primary
                        OrderStatus.COMPLETED -> Color(0xFF4CAF50)
                        OrderStatus.CANCELLED -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier
                        .background(
                            color = when (liveOrder.status) {
                                OrderStatus.ON_THE_WAY, OrderStatus.ARRIVING ->
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                OrderStatus.COMPLETED -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                                OrderStatus.CANCELLED ->
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            },
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 跑腿员信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 跑腿员头像
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = liveOrder.runnerName.first().toString(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = liveOrder.runnerName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Schedule,
                            contentDescription = "预计到达",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = liveOrder.getEstimatedTimeText(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // 操作按钮
                Row {
                    // 打电话按钮
                    liveOrder.runnerPhone?.let { phone ->
                        IconButton(
                            onClick = {
                                // 在实际应用中，这里会调用电话拨号功能
                                // val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                // context.startActivity(intent)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Phone,
                                contentDescription = "打电话",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // 发消息按钮
                    IconButton(
                        onClick = {
                            navController?.navigate("chat/${liveOrder.orderId}")
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Message,
                            contentDescription = "发消息",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 地图预览区域
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                // 这里应该显示真实的地图，但作为预览我们使用简化的UI
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = "地图",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "实时位置追踪",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = liveOrder.currentLocation?.address ?: "位置信息加载中...",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 最后更新时间和进度
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = liveOrder.getLastUpdatedText(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // 进度指示器
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .padding(horizontal = 8.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(getProgressPercentage(liveOrder.status))
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }

                Text(
                    text = "${getProgressPercentage(liveOrder.status) * 100}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * 根据订单状态计算进度百分比
 */
private fun getProgressPercentage(status: OrderStatus): Float {
    return when (status) {
        OrderStatus.WAITING_ACCEPT -> 0.1f
        OrderStatus.ACCEPTED -> 0.3f
        OrderStatus.PICKING_UP -> 0.5f
        OrderStatus.ON_THE_WAY -> 0.7f
        OrderStatus.ARRIVING -> 0.9f
        OrderStatus.COMPLETED -> 1.0f
        OrderStatus.CANCELLED -> 0.0f
    }
}