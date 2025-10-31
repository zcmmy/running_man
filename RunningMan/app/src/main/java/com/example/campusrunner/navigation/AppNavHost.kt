package com.example.campusrunner.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType // <-- 1. 导入 NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument // <-- 2. 导入 navArgument
import com.example.campusrunner.data.UserRepository
import com.example.campusrunner.ui.components.MainScaffold
import com.example.campusrunner.ui.screens.ChatScreen
import com.example.campusrunner.ui.screens.HomeScreen
import com.example.campusrunner.ui.screens.LoginScreen
import com.example.campusrunner.ui.screens.MessagesScreen
import com.example.campusrunner.ui.screens.OrderHistoryScreen
import com.example.campusrunner.ui.screens.PostScreen
import com.example.campusrunner.ui.screens.ProfileScreen
import com.example.campusrunner.ui.screens.TaskDetailScreen
import com.example.campusrunner.ui.screens.SearchScreen
import com.example.campusrunner.ui.screens.SearchResultsScreen // <-- 3. 导入 SearchResultsScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    // 监听登录状态
    val isLoggedIn by UserRepository.isLoggedIn.collectAsState()

    // 主应用框架
    MainScaffold(navController = navController) {
        NavHost(
            navController = navController,
            startDestination = if (isLoggedIn) "home" else "login"
        ) {
            composable("login") {
                LoginScreen(navController = navController)
            }
            composable("home") {
                HomeScreen(navController = navController)
            }
            composable("post") {
                PostScreen(navController = navController)
            }
            composable("messages") {
                MessagesScreen(navController = navController)
            }
            composable("profile") {
                // 检查登录状态，未登录则跳转到登录页
                LaunchedEffect(isLoggedIn) {
                    if (!isLoggedIn) {
                        navController.navigate("login") {
                            popUpTo("profile") { inclusive = true }
                        }
                    }
                }

                if (isLoggedIn) {
                    ProfileScreen(navController = navController)
                }
            }
            composable("orderHistory") {
                OrderHistoryScreen(navController = navController)
            }
            // 添加搜索页面路由
            composable(Destinations.Search) {
                SearchScreen(navController = navController)
            }
            composable("detail/{taskId}") { backStackEntry ->
                val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
                TaskDetailScreen(navController = navController, taskId = taskId)
            }
            composable("chat/{orderId}") { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                ChatScreen(navController = navController, orderId = orderId)
            }

            // --- [ 4. 添加缺失的搜索结果页路由 ] ---
            composable(
                route = "searchResults/{keyword}",
                arguments = listOf(navArgument("keyword") { type = NavType.StringType })
            ) { backStackEntry ->
                val keyword = backStackEntry.arguments?.getString("keyword") ?: ""
                SearchResultsScreen(
                    navController = navController,
                    keyword = keyword
                )
            }

            // 其他页面...
        }
    }
}
