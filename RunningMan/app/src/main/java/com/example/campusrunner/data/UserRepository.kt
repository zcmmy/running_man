package com.example.campusrunner.data

import com.example.campusrunner.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date

/**
 * 用户状态管理仓库
 * 管理登录状态和用户信息
 */
object UserRepository {
    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    /**
     * 功能：用户登录
     * 后端接入步骤：
     * 1. 实现真实的登录API调用
     * 2. 保存返回的token到RetrofitClient
     * 3. 保存用户信息到状态流
     * 调用位置：LoginScreen, 用户登录时
     */
    suspend fun login(studentId: String, password: String): Boolean {
        // TODO: 替换为实际的后端API调用
        /*
        try {
            val response = apiService.login(LoginRequest(studentId, password))
            if (response.isSuccessful && response.body() != null) {
                val loginResponse = response.body()!!

                // 保存token
                RetrofitClient.setAuthToken(loginResponse.token)

                // 更新用户状态
                _currentUser.value = loginResponse.user
                _isLoggedIn.value = true
                return true
            } else {
                // 处理登录失败
                return false
            }
        } catch (e: Exception) {
            // 处理网络错误
            return false
        }
        */

        // 模拟登录成功 - 后端API完成后删除
        val user = UserProfile(
            id = "1",
            studentId = studentId,
            name = "小明",
            avatar = null,
            phone = "138****1234",
            email = null,
            creditScore = 4.8,
            totalOrders = 8,
            totalIncome = 85.0,
            createdAt = Date()
        )

        _currentUser.value = user
        _isLoggedIn.value = true
        return true
    }

    // 登出
    fun logout() {
        _currentUser.value = null
        _isLoggedIn.value = false
    }

    /**
     * 功能：检查登录状态
     * 后端接入步骤：
     * 1. 从本地存储检查token
     * 2. 如果token有效，调用getUserProfile()获取用户信息
     * 3. 更新登录状态
     * 调用位置：App启动时，AppNavHost
     */
    fun checkLoginStatus() {
        // TODO: 从本地存储检查登录状态，例如检查token
        /*
        val token = RetrofitClient.getAuthToken()
        if (token != null) {
            // 验证token有效性，可以调用一个验证接口
            // 如果有效，调用getUserProfile()获取用户信息
            // 然后设置_isLoggedIn为true
        }
        */
    }
}