package com.example.campusrunner.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Message
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.campusrunner.ui.screens.ChatScreen
import com.example.campusrunner.ui.screens.HomeScreen
import com.example.campusrunner.ui.screens.MessagesScreen
import com.example.campusrunner.ui.screens.PostScreen
import com.example.campusrunner.ui.screens.ProfileScreen
import com.example.campusrunner.ui.screens.TaskDetailScreen
import androidx.compose.material3.Scaffold
import androidx.navigation.compose.composable
import com.example.campusrunner.ui.screens.SearchScreen // 新增导入

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    // 使用镂空图标作为默认，选中时使用填充图标
    val items = listOf(
        EnhancedNavItem(
            route = Destinations.Home,
            label = "首页",
            outlineIcon = Icons.Outlined.Home,
            filledIcon = Icons.Filled.Home
        ),
        EnhancedNavItem(
            route = Destinations.Messages,
            label = "消息",
            outlineIcon = Icons.Outlined.Message,
            filledIcon = Icons.Filled.Message
        ),
        EnhancedNavItem(
            route = Destinations.Post,
            label = "发布",
            outlineIcon = Icons.Outlined.AddCircle,
            filledIcon = Icons.Filled.AddCircle
        ),
        EnhancedNavItem(
            route = Destinations.Profile,
            label = "我的",
            outlineIcon = Icons.Outlined.Person,
            filledIcon = Icons.Filled.Person
        )
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 12.dp
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                items.forEach { item ->
                    val isSelected = currentRoute == item.route

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) item.filledIcon else item.outlineIcon,
                                contentDescription = item.label,
                                tint = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        },
                        label = {
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Destinations.Home,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Destinations.Home) {
                HomeScreen(navController = navController)
            }
            composable(Destinations.Messages) {
                MessagesScreen(navController = navController)
            }
            composable(Destinations.Post) {
                PostScreen(navController = navController)
            }
            composable(Destinations.Profile) {
                ProfileScreen(navController = navController)
            }
            // 在现有的 composable 后面添加搜索页面
            composable(Destinations.Search) {
                SearchScreen(navController = navController)
            }

            // 任务详情页面
            composable(
                route = Destinations.DetailWithArgs.RouteWithArgs,
                arguments = listOf(
                    navArgument(Destinations.DetailWithArgs.TaskId) {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val taskId = backStackEntry.arguments?.getString(Destinations.DetailWithArgs.TaskId) ?: ""
                TaskDetailScreen(
                    navController = navController,
                    taskId = taskId
                )
            }

            // 聊天页面
            composable(
                route = Destinations.ChatWithArgs.RouteWithArgs,
                arguments = listOf(
                    navArgument(Destinations.ChatWithArgs.OrderId) {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString(Destinations.ChatWithArgs.OrderId) ?: ""
                ChatScreen(
                    navController = navController,
                    orderId = orderId
                )
            }

            // 订单跟踪页面（暂时注释，等后续创建）
            /*
            composable(
                route = Destinations.OrderTrackingWithArgs.RouteWithArgs,
                arguments = listOf(
                    navArgument(Destinations.OrderTrackingWithArgs.OrderId) {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString(Destinations.OrderTrackingWithArgs.OrderId) ?: ""
                OrderTrackingScreen(
                    navController = navController,
                    orderId = orderId
                )
            }
            */
        }
    }
}

// 增强的导航项数据类，支持镂空和填充两种图标
data class EnhancedNavItem(
    val route: String,
    val label: String,
    val outlineIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val filledIcon: androidx.compose.ui.graphics.vector.ImageVector
)