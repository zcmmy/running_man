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

    // 模拟登录 - 实际开发中需要调用后端API
    suspend fun login(studentId: String, password: String): Boolean {
        // TODO: 替换为实际的后端API调用
        // val response = apiService.login(LoginRequest(studentId, password))
        // _currentUser.value = response.user
        // _isLoggedIn.value = true

        // 模拟登录成功
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

    // 检查登录状态
    fun checkLoginStatus() {
        // TODO: 从本地存储检查登录状态，例如检查token
        // 这里可以检查本地存储的token是否有效，如果有效则设置用户信息并设置isLoggedIn为true
    }
}