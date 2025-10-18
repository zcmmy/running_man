package com.example.campusrunner.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.campusrunner.model.Task
import com.example.campusrunner.model.TaskStatus
import com.example.campusrunner.model.TaskType
import com.example.campusrunner.ui.components.FilterBar
import com.example.campusrunner.ui.components.GridTaskCard
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    var searchText by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(false) }

    // 模拟任务数据
    val tasks = remember {
        listOf(
            Task(
                id = "1",
                title = "麦当劳大套餐",
                description = "校门口取餐，送至图书馆三楼 A301",
                price = 15.0,
                type = TaskType.FOOD_DELIVERY,
                status = TaskStatus.PENDING,
                location = "校门口",
                destination = "图书馆三楼 A301",
                distance = 2.0,
                publisherId = "user1",
                publisherName = "张同学",
                createdAt = Date(),
                publisherAvatar = null
            ),
            Task(
                id = "2",
                title = "打印 20 页文档",
                description = "打印店取 20 页文件，送至教学楼 502",
                price = 8.0,
                type = TaskType.PRINT,
                status = TaskStatus.IN_PROGRESS,
                location = "打印店",
                destination = "教学楼 502",
                distance = 1.0,
                publisherId = "user2",
                publisherName = "李同学",
                createdAt = Date(),
                publisherAvatar = null
            ),
            Task(
                id = "3",
                title = "取快递",
                description = "韵达快递，收件人：小明",
                price = 5.0,
                type = TaskType.EXPRESS,
                status = TaskStatus.PENDING,
                location = "快递点",
                destination = "宿舍区",
                distance = 0.5,
                publisherId = "user3",
                publisherName = "王同学",
                createdAt = Date(),
                publisherAvatar = null
            ),
            Task(
                id = "4",
                title = "买感冒药",
                description = "校医院取感冒药，送至宿舍",
                price = 25.0,
                type = TaskType.SHOPPING,
                status = TaskStatus.PENDING,
                location = "校医院",
                destination = "宿舍",
                distance = 1.5,
                publisherId = "user4",
                publisherName = "赵同学",
                createdAt = Date(),
                publisherAvatar = null
            ),
            Task(
                id = "5",
                title = "取中通快递",
                description = "取中通快递，收件人：李华",
                price = 6.0,
                type = TaskType.EXPRESS,
                status = TaskStatus.PENDING,
                location = "快递点",
                destination = "宿舍区",
                distance = 0.8,
                publisherId = "user5",
                publisherName = "刘同学",
                createdAt = Date(),
                publisherAvatar = null
            ),
            Task(
                id = "6",
                title = "买冰可乐",
                description = "超市买一瓶冰可乐，送到操场",
                price = 4.0,
                type = TaskType.SHOPPING,
                status = TaskStatus.PENDING,
                location = "超市",
                destination = "操场",
                distance = 1.2,
                publisherId = "user6",
                publisherName = "陈同学",
                createdAt = Date(),
                publisherAvatar = null
            ),
            Task(
                id = "7",
                title = "打印简历",
                description = "打印店取 5 份简历，送至宿舍",
                price = 10.0,
                type = TaskType.PRINT,
                status = TaskStatus.PENDING,
                location = "打印店",
                destination = "宿舍",
                distance = 0.3,
                publisherId = "user7",
                publisherName = "杨同学",
                createdAt = Date(),
                publisherAvatar = null
            ),
            Task(
                id = "8",
                title = "买薯片和可乐",
                description = "小卖部买零食，送到宿舍",
                price = 12.0,
                type = TaskType.SHOPPING,
                status = TaskStatus.IN_PROGRESS,
                location = "小卖部",
                destination = "宿舍",
                distance = 0.1,
                publisherId = "user8",
                publisherName = "黄同学",
                createdAt = Date(),
                publisherAvatar = null
            )
        )
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFFFA500).copy(alpha = 0.95f),
                                Color(0xFFFF8C00).copy(alpha = 0.85f)
                            )
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "推荐任务",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333),
                            modifier = Modifier.weight(1f)
                        )

                        // 搜索栏 - 调整权重和间距，确保水平对齐
                        SearchBar(
                            query = searchText,
                            onQueryChange = { searchText = it },
                            onSearch = { active = false },
                            active = active,
                            onActiveChange = { active = it },
                            modifier = Modifier.weight(1.8f),
                            placeholder = { Text("搜索跑腿任务...") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = "搜索"
                                )
                            }
                        ) {
                            // 搜索建议
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFf8f9fa)) // 更浅的背景色
        ) {
            // 筛选栏
            FilterBar()

            // 双列网格任务列表
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(tasks) { task ->
                    GridTaskCard(
                        task = task,
                        modifier = Modifier,
                        onClick = {
                            navController.navigate("detail/${task.id}")
                        }
                    )
                }
            }
        }
    }
}