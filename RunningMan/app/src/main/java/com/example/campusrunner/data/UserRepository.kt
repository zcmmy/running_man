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

    /*suspend fun login(studentId: String, password: String): Boolean {
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
    }*/

    suspend fun login(studentId: String, password: String): Boolean {
        Log.d("UserRepository", "开始登录流程")
        Log.d("UserRepository", "学号: $studentId, 密码长度: ${password.length}")

        try {
            Log.d("UserRepository", "创建登录请求")
            val loginRequest = LoginRequest(studentId, password)

            Log.d("UserRepository", "调用登录API")
            val response = apiService.login(loginRequest)
            Log.d("UserRepository", "API调用完成，响应状态: ${response.isSuccessful}")

            if (response.isSuccessful) {
                Log.d("UserRepository", "响应成功，解析响应体")
                val apiResponse = response.body()

                if (apiResponse != null) {
                    Log.d("UserRepository", "响应体解析成功，code: ${apiResponse.code}, message: ${apiResponse.message}")

                    if (apiResponse.code == 200) {
                        val loginResponse = apiResponse.data
                        Log.d("UserRepository", "登录成功，data: $loginResponse")

                        if (loginResponse != null) {
                            val token = loginResponse.token ?: ""
                            Log.d("UserRepository", "获取到token，长度: ${token.length}")

                            // 保存 token
                            RetrofitClient.setAuthToken(token)
                            this.authToken = token

                            // 更新用户状态
                            _currentUser.value = loginResponse.user
                            _isLoggedIn.value = true

                            Log.d("UserRepository", "用户信息更新完成: ${loginResponse.user}")
                            return true
                        } else {
                            Log.e("UserRepository", "登录响应中data为null")
                        }
                    } else {
                        Log.e("UserRepository", "登录失败，错误码: ${apiResponse.code}, 错误信息: ${apiResponse.message}")
                    }
                } else {
                    Log.e("UserRepository", "响应体为null")
                }
            } else {
                Log.e("UserRepository", "响应失败，错误码: ${response.code()}, 错误信息: ${response.message()}")

                // 尝试获取错误体
                try {
                    val errorBody = response.errorBody()?.string()
                    Log.e("UserRepository", "错误响应体: $errorBody")
                } catch (e: Exception) {
                    Log.e("UserRepository", "无法读取错误响应体: ${e.message}")
                }
            }

            return false
        } catch (e: Exception) {
            Log.e("UserRepository", "登录过程中发生异常: ${e.message}", e)
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
}