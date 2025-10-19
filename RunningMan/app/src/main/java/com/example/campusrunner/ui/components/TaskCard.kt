package com.example.campusrunner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.campusrunner.model.Task
import com.example.campusrunner.model.TaskType

@Composable
fun GridTaskCard(
    task: Task,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    EnhancedCard(
        modifier = modifier,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp) // 减少内边距，减少留白
        ) {
            // 图标容器 - 减少高度，图标适当放大
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp) // 从120dp减少到100dp
                    .clip(MaterialTheme.shapes.medium)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getTaskTypeIcon(task.type),
                    contentDescription = task.getTypeText(),
                    modifier = Modifier.size(48.dp), // 从48dp增加到56dp，图标更大
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(6.dp)) // 减少间距

            // 标题
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp)) // 减少间距

            // 描述
            Text(
                text = task.description,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = MaterialTheme.typography.labelSmall.lineHeight * 0.9
            )

            Spacer(modifier = Modifier.height(6.dp)) // 减少间距

            // 价格
            Text(
                text = task.getFormattedPrice(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF6B35)
            )

            Spacer(modifier = Modifier.height(6.dp)) // 减少间距

            // 元信息行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.getStatusText(),
                    style = MaterialTheme.typography.labelSmall,
                    color = when(task.status) {
                        com.example.campusrunner.model.TaskStatus.PENDING -> Color(0xFFFF6B35)
                        com.example.campusrunner.model.TaskStatus.IN_PROGRESS -> Color(0xFF2196F3)
                        com.example.campusrunner.model.TaskStatus.COMPLETED -> Color(0xFF4CAF50)
                        com.example.campusrunner.model.TaskStatus.CANCELLED -> Color(0xFF9E9E9E)
                    }
                )

                Text(
                    text = "${task.distance}km",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

/**
 * 根据任务类型获取对应的图标
 */
private fun getTaskTypeIcon(type: TaskType): ImageVector = when(type) {
    TaskType.FOOD_DELIVERY -> Icons.Filled.Restaurant
    TaskType.EXPRESS -> Icons.Filled.LocalShipping
    TaskType.PRINT -> Icons.Filled.Print
    TaskType.SHOPPING -> Icons.Filled.ShoppingCart
    TaskType.OTHER -> Icons.Filled.ShoppingCart
}