package com.example.campusrunner.data

import com.example.campusrunner.model.UserProfile
import com.example.campusrunner.network.RetrofitClient
import com.example.campusrunner.network.ApiService
import com.example.campusrunner.network.LoginRequest
import com.example.campusrunner.network.AddBalanceRequest
import com.example.campusrunner.network.SubtractBalanceRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date
import android.util.Log
import kotlin.Result // 确保导入 Result

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
                Log.w("UserRepository", "loginResponse: 登录时接收到数据 $loginResponse.data")
                if (loginResponse.code == 200) {
                    // 保存 token
                    val token = loginResponse.data?.token ?: ""
                    authToken = token // 保存在内存中
                    RetrofitClient.setAuthToken(token)

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
                // (MODIFIED) 确保 token 已设置到 RetrofitClient
                RetrofitClient.setAuthToken(authToken!!)
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

    // --- (MODIFIED): 调整现有函数 ---

    /**
     * 功能：获取当前登录用户的ID
     * 备注：这是一个非 suspend 方法，它返回当前的状态值。
     * ViewModel 加载时 _currentUser 应该已经被 checkLoginStatus() 填充了。
     */
    fun getCurrentUserId(): String? {
        return _currentUser.value?.id
    }

    /**
     * 功能：为指定用户增加余额 (跑腿员完成订单 或 发布任务失败时退款)
     * (MODIFIED: 实现了真实的API调用)
     * 调用位置：MessagesViewModel, PostScreen
     */
    suspend fun addBalance(userId: String, amount: Double) {
        // 确保增加的金额是正数
        if (amount <= 0) {
            Log.w("UserRepository", "addBalance: 尝试增加无效金额 $amount")
            return
        }

        try {
            // 1. (MODIFIED) 调用真实的API
            val response = apiService.addBalance(AddBalanceRequest(userId, amount))

            // 2. (MODIFIED) 检查响应
            if (response.isSuccessful && response.body()?.code == 200) {
                Log.d("UserRepository", "余额增加成功，正在刷新本地用户信息...")
                // 3. 刷新当前用户的个人信息
                // 仅当被修改的用户是当前登录用户时才刷新
                if (userId == _currentUser.value?.id) {
                    // (MODIFIED) 优化：不在重新fetch，而是直接在本地更新余额
                    val currentBalance = _currentUser.value?.balance ?: 0.0
                    val newBalance = currentBalance + amount
                    _currentUser.value = _currentUser.value?.copy(balance = newBalance)
                    Log.d("UserRepository", "本地余额已更新为: $newBalance")
                }
            } else {
                Log.e("UserRepository", "addBalance 失败: ${response.message()} | ${response.body()?.message}")
            }

        } catch (e: Exception) {
            Log.e("UserRepository", "addBalance 异常: ${e.message}")
            e.printStackTrace()
        }
    }


    // --- (MODIFIED) ADDED: 新增 "扣除余额" 函数 ---

    /**
     * 功能：扣除当前用户的余额 (发布任务时)
     * 调用位置：PostViewModel (或 PostScreen)
     * @return Result<Unit> - 成功或失败
     */
    suspend fun subtractBalance(amount: Double): Result<Unit> {
        if (amount <= 0) {
            Log.w("UserRepository", "subtractBalance: 尝试扣除无效金额 $amount")
            return Result.failure(Exception("扣除金额必须为正数"))
        }

        // 获
        val currentBalance = _currentUser.value?.balance ?: 0.0
        if (currentBalance < amount) {
            Log.w("UserRepository", "subtractBalance: 余额不足 (当前: $currentBalance, 需要: $amount)")
            return Result.failure(Exception("余额不足"))
        }

        try {
            // 1. 调用API
            val response = apiService.subtractBalance(SubtractBalanceRequest(amount))

            // 2. 检查响应
            if (response.isSuccessful && response.body()?.code == 200) {
                Log.d("UserRepository", "扣款成功，正在更新本地余额...")
                // 3. 在本地立即更新余额
                val newBalance = currentBalance - amount
                _currentUser.value = _currentUser.value?.copy(balance = newBalance)
                Log.d("UserRepository", "本地余额已更新为: $newBalance")
                return Result.success(Unit)
            } else {
                val errorMsg = response.body()?.message ?: "扣款失败: ${response.message()}"
                Log.e("UserRepository", "subtractBalance 失败: $errorMsg")
                return Result.failure(Exception(errorMsg))
            }

        } catch (e: Exception) {
            Log.e("UserRepository", "subtractBalance 异常: ${e.message}")
            e.printStackTrace()
            return Result.failure(e)
        }
    }

    // --- END: 新增函数 ---
}

