package com.example.campusrunner.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable // [!!] 导入
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext // [!!] 导入
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.campusrunner.utils.MapUtils // [!!] 导入
import com.example.campusrunner.viewmodels.AcceptTaskState
import com.example.campusrunner.viewmodels.TaskDetailViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    navController: NavController,
    taskId: String
) {
    // 初始化ViewModel
    val viewModel: TaskDetailViewModel = viewModel()
    val task = viewModel.taskState.value
    val isLoading = viewModel.loadingState.value
    val error = viewModel.errorState.value
    val acceptTaskState = viewModel.acceptTaskState.value

    // Snackbar状态管理
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // 加载任务详情
    LaunchedEffect(taskId) {
        viewModel.loadTaskDetail(taskId)
    }

    // 处理接单状态变化
    LaunchedEffect(acceptTaskState) {
        when (acceptTaskState) {
            is AcceptTaskState.Success -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("接单成功！")
                }
            }
            is AcceptTaskState.Error -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(acceptTaskState.message)
                }
            }
            else -> {}
        }
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
            androidx.compose.material3.TopAppBar(
                title = {
                    Text(
                        text = "任务详情",
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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> {
                    // 加载中状态
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("加载任务详情中...")
                    }
                }
                task != null -> {
                    // 显示任务详情
                    TaskDetailContent(
                        task = task,
                        onAcceptTask = {
                            viewModel.acceptTask(task.id)
                        },
                        acceptTaskState = acceptTaskState
                    )
                }
                else -> {
                    // 错误或空状态
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = error ?: "任务不存在",
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadTaskDetail(taskId) }) {
                            Text("重新加载")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TaskDetailContent(
    task: com.example.campusrunner.model.Task,
    onAcceptTask: () -> Unit,
    acceptTaskState: AcceptTaskState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // 任务状态横幅
        TaskStatusBanner(task)

        // 任务基本信息卡片
        TaskBasicInfoCard(task)

        // 任务详情卡片
        TaskDetailsCard(task)

        // 发布者信息卡片
        PublisherInfoCard(task)

        // 操作按钮区域
        ActionButtons(
            task = task,
            onAcceptTask = onAcceptTask,
            acceptTaskState = acceptTaskState
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun TaskStatusBanner(task: com.example.campusrunner.model.Task) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(task.getStatusColor())
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = task.getStatusText(),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}

@Composable
fun TaskBasicInfoCard(task: com.example.campusrunner.model.Task) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = task.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.getFormattedPrice(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF6B35)
                )

                Text(
                    text = task.getTypeText(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun TaskDetailsCard(task: com.example.campusrunner.model.Task) {
    // [!!] 获取 Context
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "任务信息",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // 取货地点
            DetailRow(
                icon = Icons.Filled.LocationOn,
                title = "取货地点",
                content = task.location,
                // [!!] 添加点击事件和 modifier
                modifier = Modifier.clickable {
                    MapUtils.showAddressOnWebMap(context, task.location)
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 送货地点
            DetailRow(
                icon = Icons.Filled.LocationOn,
                title = "送货地点",
                content = task.destination,
                // [!!] 添加点击事件和 modifier
                modifier = Modifier.clickable {
                    MapUtils.showAddressOnWebMap(context, task.destination)
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 发布时间
            DetailRow(
                icon = Icons.Filled.Schedule,
                title = "发布时间",
                content = task.getCreatedTimeText()
                // (不需要 modifier，它会使用默认值 Modifier)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 距离信息
            // [!!] 注意：这个 getFormattedDistance() 可能不再准确，
            // 因为我们删除了原生地图服务。如果它显示为0或奇怪的值，
            // 你可以考虑隐藏这一行，或者将其 content 改为 "N/A"。
            DetailRow(
                icon = Icons.Filled.LocationOn,
                title = "距离",
                content = task.getFormattedDistance()
            )

            // 预计完成时间
            task.estimatedTime?.let {
                Spacer(modifier = Modifier.height(12.dp))
                DetailRow(
                    icon = Icons.Filled.Schedule,
                    title = "预计完成时间",
                    content = task.getEstimatedTimeText()
                )
            }

            // 特殊要求
            task.specialRequirements?.let { requirements ->
                Spacer(modifier = Modifier.height(12.dp))
                DetailRow(
                    icon = Icons.Filled.Star,
                    title = "特殊要求",
                    content = requirements
                )
            }
        }
    }
}

@Composable
fun PublisherInfoCard(task: com.example.campusrunner.model.Task) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "发布者信息",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
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
                        text = task.publisherName.first().toString(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = task.publisherName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "信用分",
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "信用分: ${task.publisherRating}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 联系电话（如果有）
            task.contactPhone?.let { phone ->
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Phone,
                        contentDescription = "电话",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = phone,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun ActionButtons(
    task: com.example.campusrunner.model.Task,
    onAcceptTask: () -> Unit,
    acceptTaskState: AcceptTaskState
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        when (task.status) {
            com.example.campusrunner.model.TaskStatus.PENDING -> {
                Button(
                    onClick = onAcceptTask,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = acceptTaskState !is AcceptTaskState.Loading
                ) {
                    if (acceptTaskState is AcceptTaskState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("接单中...")
                    } else {
                        Text(
                            text = "立即接单",
                            style = MaterialTheme.typography.bodyLarge,
                            fontSize = 18.sp
                        )
                    }
                }
            }
            com.example.campusrunner.model.TaskStatus.IN_PROGRESS -> {
                Text(
                    text = "任务进行中",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                        )
                        .padding(16.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }
            com.example.campusrunner.model.TaskStatus.COMPLETED -> {
                Text(
                    text = "任务已完成",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                        )
                        .padding(16.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }
            com.example.campusrunner.model.TaskStatus.CANCELLED -> {
                Text(
                    text = "任务已取消",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                        )
                        .padding(16.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

// [!!] 修正 DetailRow 函数定义
@Composable
fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    content: String,
    modifier: Modifier = Modifier // [!!] 添加 modifier 参数并设置默认值
) {
    Row(
        modifier = modifier.fillMaxWidth(), // [!!] 应用 modifier
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(end = 12.dp)
                .size(20.dp)
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

