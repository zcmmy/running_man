package com.example.campusrunner.data

import com.example.campusrunner.model.Task
import com.example.campusrunner.model.TaskStatus
import com.example.campusrunner.network.ApiService
import com.example.campusrunner.network.RetrofitClient
import com.example.campusrunner.network.TaskRequest
import kotlin.Result // 导入 Result 类

/**
 * 任务数据仓库 - 接入后端API
 */
object TaskRepository {

    private val apiService: ApiService = RetrofitClient.apiService

    /**
     * 从服务器获取任务详情
     *
     * 调用位置：TaskDetailViewModel (当创建时)
     * API: GET /api/tasks/{taskId}
     */
    suspend fun getTaskById(
        taskId: String,
    ): Result<Task> {
        return try {
            val response = apiService.getTaskDetail(taskId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("获取任务详情失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("网络错误: ${e.message}"))
        }
    }


    /**
     * 功能：接单操作
     *
     * 调用位置：TaskDetailViewModel, 用户点击接单按钮时
     * API: POST /api/tasks/{id}/accept
     */
    suspend fun acceptTask(
        taskId: String,
        // runnerId 通常由后端的token自动获取，所以参数中移除
    ): Result<String> {
        return try {
            val response = apiService.acceptTask(taskId)
            if (response.isSuccessful && response.body()?.code == 200) {
                Result.success(response.body()?.data ?: "接单成功")
            } else {
                Result.failure(Exception("接单失败: ${response.body()?.message ?: "未知错误"}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("网络错误: ${e.message}"))
        }
    }


    /**
     * 功能：发布新任务到服务器
     *
     * 调用位置：PostViewModel, 用户发布任务时
     * API: POST /api/tasks
     */
    suspend fun createTask(
        taskRequest: TaskRequest,
    ): Result<String> {
        return try {
            val response = apiService.createTask(taskRequest)
            if (response.isSuccessful && response.body()?.code == 200) {
                Result.success(response.body()?.data ?: "发布成功")
            } else {
                Result.failure(Exception("发布失败: ${response.body()?.message ?: "未知错误"}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("网络错误: ${e.message}"))
        }
    }

    /**
     * 获取任务状态更新 - 用于实时更新任务状态
     */
    fun subscribeToTaskUpdates(
        taskId: String,
        onStatusUpdate: (TaskStatus) -> Unit
    ) {
        // TODO: 在实际应用中，这里会使用WebSocket或轮询来获取实时状态更新
        // 暂时返回空实现
    }

    /**
     * 从服务器获取任务列表
     * API: GET /api/tasks
     */
    suspend fun getTasksFromServer(
        page: Int = 1,
        limit: Int = 20,
        type: String? = null,
        location: String? = null,
    ): Result<List<Task>> {
        return try {
            // ApiService.getTasks 是 suspend 函数，可以直接调用
            val response = apiService.getTasks(page, limit, type, location)
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("获取任务列表失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("网络错误: ${e.message}"))
        }
    }
}

