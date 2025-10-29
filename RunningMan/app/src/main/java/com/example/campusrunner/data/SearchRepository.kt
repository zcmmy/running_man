package com.example.campusrunner.data

import android.util.Log // <-- 1. 添加 Log 导入
import com.example.campusrunner.model.SearchHistory
import com.example.campusrunner.network.SearchHistoryRequest
import com.example.campusrunner.model.Task
import com.example.campusrunner.network.ApiService
import com.example.campusrunner.network.RetrofitClient
import kotlin.Result

/**
 * 搜索数据仓库 - 接入后端API
 */
object SearchRepository {

    private val apiService: ApiService = RetrofitClient.apiService
    private const val TAG = "SearchRepository" // <-- 2. 添加 TAG

    // 移除了 mockSearchHistory 模拟数据

    /**
     * 功能：获取搜索历史记录
     *
     * 调用位置：HomeViewModel.loadSearchHistory(), SearchScreen
     * API: GET /api/search/history
     */
    suspend fun getSearchHistory(
        userId: String,
        limit: Int = 10
    ): Result<List<SearchHistory>> {
        Log.d(TAG, "getSearchHistory: 正在请求... UserId: $userId, Limit: $limit") // <-- 添加日志
        return try {
            val response = apiService.getSearchHistory(userId, limit)
            if (response.isSuccessful && response.body() != null) {
                val histories = response.body()!!.histories
                Log.d(TAG, "getSearchHistory: 成功。获取到 ${histories.size} 条历史。") // <-- 添加日志
                Result.success(histories)
            } else {
                Log.w(TAG, "getSearchHistory: 失败。代码: ${response.code()}, 信息: ${response.message()}") // <-- 添加日志
                Result.failure(Exception("获取搜索历史失败: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getSearchHistory: 网络异常", e) // <-- 添加日志
            Result.failure(Exception("网络错误 (getSearchHistory): ${e.message}"))
        }
    }

    /**
     * 功能：添加搜索历史（执行搜索时调用）
     * (这是 HomeViewModel 需要的函数)
     *
     * 调用位置：HomeViewModel.performSearch()
     * API: POST /api/search/history
     */
    suspend fun addSearchHistory(
        userId: String,
        keyword: String
    ): Result<String> {
        Log.d(TAG, "addSearchHistory: 正在添加 '$keyword' (用户: $userId)") // <-- 添加日志
        val request = SearchHistoryRequest(keyword = keyword, userId = userId)
        return try {
            val response = apiService.addSearchHistory(request)
            if (response.isSuccessful && response.body()?.code == 200) {
                Log.d(TAG, "addSearchHistory: 成功。") // <-- 添加日志
                Result.success(response.body()?.data ?: "添加成功")
            } else {
                Log.w(TAG, "addSearchHistory: 失败。代码: ${response.code()}, 信息: ${response.body()?.message}") // <-- 添加日志
                Result.failure(Exception("添加搜索历史失败: ${response.body()?.message ?: response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "addSearchHistory: 网络异常", e) // <-- 添加日志
            Result.failure(Exception("网络错误 (addSearchHistory): ${e.message}"))
        }
    }


    /**
     * 功能：删除单个搜索历史
     *
     * 调用位置：SearchScreen.kt, HomeViewModel.deleteSearchHistory()
     * API: DELETE /api/search/history/{historyId}
     */
    suspend fun deleteSearchHistory(
        historyId: String
    ): Result<String> {
        Log.d(TAG, "deleteSearchHistory: 正在删除: $historyId") // <-- 添加日志
        return try {
            val response = apiService.deleteSearchHistory(historyId)
            if (response.isSuccessful && response.body()?.code == 200) {
                Log.d(TAG, "deleteSearchHistory: 成功。") // <-- 添加日志
                Result.success(response.body()?.data ?: "删除成功")
            } else {
                Log.w(TAG, "deleteSearchHistory: 失败。代码: ${response.code()}, 信息: ${response.body()?.message}") // <-- 添加日志
                Result.failure(Exception("删除搜索历史失败: ${response.body()?.message ?: response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "deleteSearchHistory: 网络异常", e) // <-- 添加日志
            Result.failure(Exception("网络错误 (deleteSearchHistory): ${e.message}"))
        }
    }

    /**
     * 功能：清空搜索历史
     *
     * 调用位置：SearchScreen.kt, HomeViewModel.clearSearchHistory()
     * API: DELETE /api/search/history?userId={userId}
     */
    suspend fun clearSearchHistory(
        userId: String
    ): Result<String> {
        Log.d(TAG, "clearSearchHistory: 正在清空用户 $userId 的历史") // <-- 添加日志
        return try {
            val response = apiService.clearSearchHistory(userId)
            if (response.isSuccessful && response.body()?.code == 200) {
                Log.d(TAG, "clearSearchHistory: 成功。") // <-- 添加日志
                Result.success(response.body()?.data ?: "清空成功")
            } else {
                Log.w(TAG, "clearSearchHistory: 失败。代码: ${response.code()}, 信息: ${response.body()?.message}") // <-- 添加日志
                Result.failure(Exception("清空搜索历史失败: ${response.body()?.message ?: response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "clearSearchHistory: 网络异常", e) // <-- 添加日志
            Result.failure(Exception("网络错误 (clearSearchHistory): ${e.message}"))
        }
    }

    /**
     * 功能：搜索任务
     *
     * 调用位置：HomeViewModel.performSearch(), 用户执行搜索时
     * API: GET /api/tasks?search={keyword}&limit=8
     */
    suspend fun searchTasks(
        keyword: String
    ): Result<List<Task>> {
        Log.d(TAG, "searchTasks: 正在搜索任务... 关键字: '$keyword', 限制: 8") // <-- 3. 添加日志
        return try {
            // [修复] 调用 ApiService.kt 中 'getTasks' 端点，并传入 search 参数
            // 限制8个结果
            val response = apiService.getTasks(
                search = keyword,
                limit = 8,
                page = 1
            )

            if (response.isSuccessful) {
                val tasks = response.body() ?: emptyList()
                Log.d(TAG, "searchTasks: 成功。获取到 ${tasks.size} 个任务。") // <-- 4. 添加日志
                Result.success(tasks)
            } else {
                Log.w(TAG, "searchTasks: 失败。代码: ${response.code()}, 信息: ${response.message()}") // <-- 5. 添加日志
                Result.failure(Exception("搜索失败: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "searchTasks: 网络异常", e) // <-- 6. 添加日志 (e 包含了详细堆栈)
            Result.failure(Exception("网络错误 (searchTasks): ${e.message}"))
        }
    }

    // 移除了 getAllMockTasksForSearch() 模拟辅助函数
}

