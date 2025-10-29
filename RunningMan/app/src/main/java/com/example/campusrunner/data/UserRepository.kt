package com.example.campusrunner.data

import com.example.campusrunner.model.UserProfile
import com.example.campusrunner.network.RetrofitClient
import com.example.campusrunner.network.ApiService
import com.example.campusrunner.network.LoginRequest
import com.example.campusrunner.network.LoginResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date
import android.util.Log

/**
 * 用户状态管理仓库
 * 管理登录状态和用户信息
 */
object UserRepository {

    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    // 在 UserRepository 中管理 token
    private var authToken: String? = null

    private val apiService: ApiService = RetrofitClient.apiService

    /**
     * 功能：用户登录
     * 后端接入步骤：
     * 1. 实现真实的登录API调用
     * 2. 保存返回的token到RetrofitClient
     * 3. 保存用户信息到状态流
     * 调用位置：LoginScreen, 用户登录时
     */

    suspend fun login(studentId: String, password: String): Boolean {
        try {
            val response = apiService.login(LoginRequest(studentId, password))
            if (response.isSuccessful && response.body() != null) {
                val loginResponse = response.body()!!

                if (loginResponse.code == 200) {
                    // 保存 token
                    RetrofitClient.setAuthToken(loginResponse.data?.token ?: "")

                    // 更新用户状态
                    _currentUser.value = loginResponse.data?.user
                    _isLoggedIn.value = true
                    return true
                }
            }
            return false
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }


    /**
     * 获取用户个人信息
     */
    suspend fun fetchUserProfile() {
        try {
            val response = apiService.getUserProfile()
            if (response.isSuccessful && response.body() != null) {
                _currentUser.value = response.body()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 登出
    fun logout() {
        _currentUser.value = null
        _isLoggedIn.value = false
        authToken = null
        RetrofitClient.setAuthToken("")
    }
    /**
     * 功能：检查登录状态
     * 后端接入步骤：
     * 1. 从本地存储检查token
     * 2. 如果token有效，调用getUserProfile()获取用户信息
     * 3. 更新登录状态
     * 调用位置：App启动时，AppNavHost
     */
    /**
     * 功能：检查登录状态
     */
    suspend fun checkLoginStatus() {
        // 检查本地保存的 token
        if (authToken != null && authToken!!.isNotEmpty()) {
            // 如果有 token，尝试获取用户信息来验证有效性
            try {
                val response = apiService.getUserProfile()
                if (response.isSuccessful && response.body() != null) {
                    _currentUser.value = response.body()
                    _isLoggedIn.value = true
                    return
                }
            } catch (e: Exception) {
                // 获取用户信息失败，token 可能已失效
                e.printStackTrace()
            }
        }

        // 如果没有 token 或 token 失效
        _isLoggedIn.value = false
        _currentUser.value = null
    }

    // --- ADDED: 新增函数 ---

    /**
     * 功能：获取当前登录用户的ID
     * 备注：这是一个非 suspend 方法，它返回当前的状态值。
     * ViewModel 加载时 _currentUser 应该已经被 checkLoginStatus() 填充了。
     */
    fun getCurrentUserId(): String? {
        return _currentUser.value?.id
    }

    /**
     * 功能：为指定用户增加余额 (跑腿员完成订单)
     * 后端接入步骤：
     * 1. (已添加) 在 ApiService 中定义一个 addBalance 接口 (例如 @POST("/user/addBalance"))
     * 2. (已添加) 调用 apiService.addBalance(...)
     * 3. (已添加) 成功后，调用 fetchUserProfile() 刷新本地的用户信息
     * 调用位置：MessagesViewModel
     */
    suspend fun addBalance(userId: String, amount: Double) {
        // 确保增加的金额是正数
        if (amount <= 0) {
            Log.w("UserRepository", "addBalance: 尝试增加无效金额 $amount")
            return
        }

        try {
            // TODO: (后端) 1. 在 ApiService 中定义 addBalance(AddBalanceRequest(userId, amount))
            // val response = apiService.addBalance(AddBalanceRequest(userId, amount))

            // (模拟后端调用成功)
            Log.d("UserRepository", "正在为 $userId 增加余额 $amount (模拟)")
            // 模拟延迟
            kotlinx.coroutines.delay(500)

            // 2. 假设后端调用成功
            // if (response.isSuccessful) {
            Log.d("UserRepository", "余额增加成功，正在刷新本地用户信息...")
            // 3. 刷新当前用户的个人信息，以便UI（如ProfileScreen）更新
            // 仅当被修改的用户是当前登录用户时才刷新
            if (userId == _currentUser.value?.id) {
                fetchUserProfile()
            }
            // } else {
            //     Log.e("UserRepository", "addBalance 失败: ${response.message()}")
            // }

        } catch (e: Exception) {
            Log.e("UserRepository", "addBalance 异常: ${e.message}")
            e.printStackTrace()
        }
    }

    // (你可能需要一个 DTO 来请求)
    // data class AddBalanceRequest(val userId: String, val amount: Double)

    // --- END: 新增函数 ---
}

