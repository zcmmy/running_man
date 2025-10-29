package com.example.campusrunner.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow // 导入 shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.campusrunner.model.Task
import com.example.campusrunner.viewmodels.HomeViewModel
import java.util.Date // 确保导入 Date

/**
 * 搜索结果页面
 *
 * @param navController 导航控制器
 * @param keyword 搜索关键字
 */
@OptIn(ExperimentalMaterial3Api::class) // 启用 Material 3 实验性 API
@Composable
fun SearchResultsScreen(
    navController: NavController,
    keyword: String
) {
    // 共用 HomeViewModel 来执行搜索
    val viewModel: HomeViewModel = viewModel()
    val tasks by viewModel.tasks
    val loadingState by viewModel.loadingState
    val errorState by viewModel.errorState // 委托属性

    // 当页面加载时，使用关键字执行搜索
    LaunchedEffect(key1 = keyword) {
        viewModel.performSearch(keyword)
    }

    Scaffold(
        topBar = {
            // --- [ 修改：使用自定义的 Row 替换 TopAppBar ] ---
            // 我们使用 Surface 和 Row 手动构建顶部栏，以完全控制布局
            Surface(
                modifier = Modifier
                    .fillMaxWidth() // 宽度占满
                    .shadow(4.dp), // 添加阴影
                color = MaterialTheme.colorScheme.surface // 设置背景色
            ) {
                // 使用 Row 来手动布局
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        // [ 参数 ] 1. (重要) 为系统状态栏（时间、电量）添加顶部内边距。
                        .padding(WindowInsets.statusBars.asPaddingValues())
                        // [ 参数 ] 2. (重要) 在状态栏内边距 *之后*，设置内容区域的固定高度。
                        .height(56.dp)
                        // 为按钮和文本添加一些水平内边距
                        .padding(horizontal = 4.dp),
                    // [ 参数 ] 3. 在 56.dp 的高度内垂直居中对齐。这会修复内容靠近底部的问题。
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 返回按钮
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            // [ 参数 ] 4. 增大图标尺寸
                            modifier = Modifier.size(28.dp),
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "返回" // 简体中文
                        )
                    }
                    // 标题
                    Text(
                        text = "“$keyword”的搜索结果",
                        modifier = Modifier.padding(start = 12.dp), // 标题和按钮的间距
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        // [ 参数 ] 5. 增大字体大小
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            // --- [ 修改结束 ] ---
        },
        // --- [美化] ---
        // 设置统一的背景色
        containerColor = Color(0xFFF8F9FA) // 使用一个柔和的浅灰色背景
    ) { paddingValues ->

        // --- [修改] ---
        // 从 when 语句中取出局部变量，修复 'Smart cast' 错误
        val currentError = errorState

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // 使用 Scaffold 提供的 padding
        ) {
            when {
                loadingState -> {
                    // 加载中
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                currentError != null -> { // <-- [修复] 使用局部变量
                    // 错误状态
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = "错误", // 简体中文
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "加载失败", // 简体中文
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = currentError, // <-- [修复] 使用局部变量
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                tasks.isEmpty() -> {
                    // 空状态
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = "无结果", // 简体中文
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "没有找到相关任务", // 简体中文
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "尝试更换一个搜索词吧", // 简体中文
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                else -> {
                    // 任务列表
                    // --- [美化] ---
                    // 添加了 contentPadding
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp) // 统一的卡片间距
                    ) {
                        items(tasks, key = { it.id }) { task ->
                            ListTaskCard(task = task, onClick = {
                                navController.navigate("detail/${task.id}")
                            })
                        }
                    }
                }
            }
        }
    }
}

/**
 * 搜索结果中使用的列表任务卡片 (美化后)
 * (这是一个新的 Composable，专为列表优化)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ListTaskCard(
    task: Task,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min), // 确保卡片高度自适应
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.medium // 使用 M3 圆角
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // 左侧：任务信息
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(), // 填充高度
                verticalArrangement = Arrangement.SpaceBetween // 垂直方向两端对齐
            ) {
                // 顶部：类型和标题
                Column {
                    Text(
                        text = task.getTypeText(), // <-- 调用正确的函数
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                shape = MaterialTheme.shapes.small
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // 底部：时间和地点
                Column {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "地点: ${task.location}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "时间: ${task.getCreatedTimeText()}", // <-- 调用正确的函数
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // 右侧：价格
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(start = 12.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center // 垂直居中
            ) {
                Text(
                    text = task.getFormattedPrice(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error // 使用醒目的颜色
                )
            }
        }
    }
}

/*
[ 辅助函数 ]
请确保这些函数存在于你的 `Task.kt` data class 中：

// 示例实现（在 Task.kt 中）:
fun Task.getTypeText(): String {
    return when (type) {
        TaskType.FOOD_DELIVERY -> "外卖代取"
        TaskType.EXPRESS -> "快递代取"
        TaskType.PRINT -> "打印复印"
        TaskType.SHOPPING -> "代购"
        TaskType.OTHER -> "其他"
    }
}

// 示例实现（在 Task.kt 中）:
fun Task.getCreatedTimeText(): String {
    val diff = System.currentTimeMillis() - this.createdAt.time
    val minutes = diff / (1000 * 60)
    val hours = minutes / 60
    val days = hours / 24

    return when {
        minutes < 1 -> "刚刚"
        minutes < 60 -> "${minutes}分钟前"
        hours < 24 -> "${hours}小时前"
        else -> "${days}天前"
    }
}

fun Task.getFormattedPrice(): String = "¥${price.toInt()}"

*/

