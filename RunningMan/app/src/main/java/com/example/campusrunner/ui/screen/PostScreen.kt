package com.example.campusrunner.ui.screens

import android.util.Log // (MODIFIED) 新增 Log 导入
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Money
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.campusrunner.data.TaskRepository
import com.example.campusrunner.data.UserRepository
import com.example.campusrunner.model.TaskStatus
import com.example.campusrunner.model.TaskType
import com.example.campusrunner.network.TaskRequest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostScreen(navController: NavController? = null) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var destination by remember { mutableStateOf("") }

    // (MODIFIED) 新增状态管理
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val currentUser by UserRepository.currentUser.collectAsState()

    // (MODIFIED) 获取仓库实例
    val userRepository = UserRepository
    val taskRepository = TaskRepository

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "发布任务",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController?.popBackStack() }) {
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
                .verticalScroll(rememberScrollState())
        ) {
            // 发布说明卡片
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "发布任务说明",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "请详细描述您的需求，清晰的描述有助于跑腿员更好地为您服务",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    // (MODIFIED) 显示当前余额
                    Text(
                        text = "当前余额: ¥${currentUser?.balance ?: 0.0}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            // 任务信息表单
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "任务信息",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // 任务标题
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("任务标题") },
                        placeholder = { Text("例如：取快递、买奶茶") },
                        leadingIcon = {
                            Icon(Icons.Filled.Description, null)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 任务描述
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("详细描述") },
                        placeholder = { Text("请详细描述您的需求...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        singleLine = false
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 价格
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("报酬金额") },
                        placeholder = { Text("例如：15") },
                        leadingIcon = {
                            Icon(Icons.Filled.Money, null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        // (MODIFIED) 数字键盘和错误状态
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = errorMessage?.contains("金额") == true || errorMessage?.contains("余额") == true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 取货地点
                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            label = { Text("取货地点") },
                            placeholder = { Text("例如：南大门") },
                            leadingIcon = {
                                Icon(Icons.Filled.LocationOn, null)
                            },
                            modifier = Modifier.weight(1f)
                        )

                        // 送货地点
                        OutlinedTextField(
                            value = destination,
                            onValueChange = { destination = it },
                            label = { Text("送货地点") },
                            placeholder = { Text("例如：图书馆") },
                            leadingIcon = {
                                Icon(Icons.Filled.LocalShipping, null)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // (MODIFIED) 在按钮下方显示错误消息
            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    fontWeight = FontWeight.Bold
                )
            }

            // 发布按钮
            Button(
                onClick = {
                    // (MODIFIED) 发布任务的完整逻辑
                    val taskPrice = price.toDoubleOrNull()
                    val currentBalance = currentUser?.balance ?: 0.0

                    // 0. 清除旧错误并开始加载
                    errorMessage = null
                    isLoading = true

                    // 1. 验证金额
                    if (taskPrice == null || taskPrice <= 0) {
                        errorMessage = "请输入有效的金额"
                        isLoading = false
                        return@Button
                    }

                    // 2. 检查余额 (前端检查)
                    if (currentBalance < taskPrice) {
                        errorMessage = "余额不足 (当前: ¥$currentBalance)"
                        isLoading = false
                        return@Button
                    }

                    // 3. 创建 TaskRequest (提前创建)
                    val taskRequest = TaskRequest(
                        title = title,
                        description = description,
                        price = taskPrice,
                        // TODO: 您需要一个UI来选择类型，这里暂时硬编码
                        type = TaskType.FOOD_DELIVERY,
                        location = location,
                        destination = destination,
                    )
                    Log.d("PostScreen", "TaskRequest: $taskRequest")

                    scope.launch {
                        try {
                            // 4. (关键) 先调用扣款
                            val subtractResult = userRepository.subtractBalance(taskPrice)

                            if (subtractResult.isSuccess) {
                                // 5. 扣款成功，再发布任务
                                val createResult = taskRepository.createTask(taskRequest)

                                if (createResult.isSuccess) {
                                    // 6. (完美!) 成功发布
                                    isLoading = false
                                    navController?.popBackStack()
                                } else {
                                    // 7. (错误处理) 任务发布失败，必须退款！
                                    errorMessage = createResult.exceptionOrNull()?.message ?: "任务发布失败"
                                    Log.e("PostScreen", "任务发布失败，正在退款...")
                                    // (关键!) 调用退款
                                    // (MODIFIED) 添加安全检查
                                    currentUser?.id?.let { userId ->
                                        userRepository.addBalance(userId, taskPrice)
                                    } ?: run {
                                        Log.e("PostScreen", "退款失败：无法获取用户ID")
                                    }
                                    isLoading = false
                                }
                            } else {
                                // 8. (错误处理) 扣款失败 (例如：后端再次验证余额不足)
                                errorMessage = subtractResult.exceptionOrNull()?.message ?: "扣款失败"
                                isLoading = false
                            }
                        } catch (e: Exception) {
                            errorMessage = e.message ?: "发生未知错误"
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                // (MODIFIED) 添加加载状态
                enabled = title.isNotEmpty() && description.isNotEmpty() && price.isNotEmpty() && !isLoading
            ) {
                // (MODIFIED) 根据加载状态显示不同内容
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = "确认支付并发布 (¥${price.ifEmpty { "0" }})",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

