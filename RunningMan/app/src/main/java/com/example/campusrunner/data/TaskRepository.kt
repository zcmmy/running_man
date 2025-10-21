package com.example.campusrunner.data

import com.example.campusrunner.model.Task
import com.example.campusrunner.model.TaskStatus
import com.example.campusrunner.model.TaskType
import com.example.campusrunner.network.ApiService
import com.example.campusrunner.network.RetrofitClient
import com.example.campusrunner.network.TaskRequest
import java.util.Date

/**
 * 任务数据仓库 - 包含模拟数据和网络请求
 */
object TaskRepository {

    // 原有的模拟数据保持不变
    private val mockTasks = listOf(
        Task(
            id = "1",
            title = "麦当劳大套餐",
            description = "校门口取餐，送至图书馆三楼 A301。请确保食物保持温热状态，饮料不要摇晃。",
            price = 15.0,
            type = TaskType.FOOD_DELIVERY,
            status = TaskStatus.PENDING,
            location = "学校南门麦当劳",
            destination = "图书馆三楼 A301",
            distance = 2.0,
            publisherId = "user1",
            publisherName = "张同学",
            publisherAvatar = null,
            publisherRating = 4.8,
            createdAt = Date(System.currentTimeMillis() - 10 * 60 * 1000),
            acceptedAt = null,
            completedAt = null,
            runnerId = null,
            runnerName = null
        ),
        Task(
            id = "2",
            title = "打印 20 页文档",
            description = "打印店取 20 页文件，送至教学楼 502。需要双面打印，黑色墨水。",
            price = 8.0,
            type = TaskType.PRINT,
            status = TaskStatus.IN_PROGRESS,
            location = "图书馆打印店",
            destination = "教学楼 502",
            distance = 1.0,
            publisherId = "user2",
            publisherName = "李同学",
            publisherAvatar = null,
            publisherRating = 4.9,
            createdAt = Date(System.currentTimeMillis() - 30 * 60 * 1000),
            acceptedAt = Date(System.currentTimeMillis() - 15 * 60 * 1000),
            completedAt = null,
            runnerId = "runner1",
            runnerName = "王跑腿"
        ),
        Task(
            id = "3",
            title = "取快递包裹",
            description = "韵达快递，收件人：小明。包裹较大，需要带小推车。",
            price = 5.0,
            type = TaskType.EXPRESS,
            status = TaskStatus.PENDING,
            location = "校门口快递点",
            destination = "韵苑学生公寓 3栋 201",
            distance = 0.5,
            publisherId = "user3",
            publisherName = "王同学",
            publisherAvatar = null,
            publisherRating = 4.7,
            createdAt = Date(System.currentTimeMillis() - 5 * 60 * 1000),
            acceptedAt = null,
            completedAt = null,
            runnerId = null,
            runnerName = null
        ),
        Task(
            id = "4",
            title = "买感冒药和温度计",
            description = "校医院购买感冒药和电子温度计，需要保留小票。",
            price = 25.0,
            type = TaskType.SHOPPING,
            status = TaskStatus.PENDING,
            location = "校医院药房",
            destination = "紫菘学生公寓 5栋 305",
            distance = 1.5,
            publisherId = "user4",
            publisherName = "赵同学",
            publisherAvatar = null,
            publisherRating = 4.8,
            createdAt = Date(System.currentTimeMillis() - 20 * 60 * 1000),
            acceptedAt = null,
            completedAt = null,
            runnerId = null,
            runnerName = null
        )
    )

    private val apiService: ApiService = RetrofitClient.apiService

    /**
     * 获取任务详情 - 优先从服务器获取，失败则使用模拟数据
     * 后端接入步骤：
     * 1. 取消注释 getTaskDetailFromServer(taskId, onSuccess, onError) 调用
     * 2. 在 getTaskDetailFromServer 中实现真实的后端API调用
     * 3. 移除模拟数据逻辑
     * 调用位置：TaskDetailScreen.kt, 任务详情页面
     *
     * 后端API实现示例：
     * GET /api/tasks/{taskId}
     * 请求示例：GET /api/tasks/1
     * 响应示例：
     * {
     *   "id": "1",
     *   "title": "麦当劳大套餐",
     *   "description": "校门口取餐，送至图书馆三楼 A301。请确保食物保持温热状态，饮料不要摇晃。",
     *   "price": 15.0,
     *   "type": "FOOD_DELIVERY",
     *   "status": "PENDING",
     *   "location": "学校南门麦当劳",
     *   "destination": "图书馆三楼 A301",
     *   "distance": 2.0,
     *   "publisherId": "user1",
     *   "publisherName": "张同学",
     *   "publisherAvatar": null,
     *   "publisherRating": 4.8,
     *   "createdAt": "2024-01-20T10:00:00Z",
     *   "acceptedAt": null,
     *   "completedAt": null,
     *   "runnerId": null,
     *   "runnerName": null
     * }
     */
    fun getTaskById(
        taskId: String,
        onSuccess: (Task) -> Unit,
        onError: (String) -> Unit
    ) {
        // TODO: 在实际应用中，这里应该优先从服务器获取
        // 目前先使用模拟数据，后续可以改为网络请求

        Thread {
            Thread.sleep(500)
            val task = mockTasks.find { it.id == taskId }
            if (task != null) {
                onSuccess(task)
            } else {
                // TODO: 当后端API准备好后，取消注释以下代码
                // 优先从服务器获取任务详情
                // getTaskDetailFromServer(taskId, onSuccess, onError)

                // 临时模拟：如果模拟数据中没有，返回错误
                onError("任务不存在或已被删除")
            }
        }.start()
    }

    /**
     * 从服务器获取任务详情 - 私有函数，被getTaskById调用
     * 后端接入步骤：
     * 1. 取消注释 apiService.getTaskDetail(taskId).execute() 调用
     * 2. 处理网络响应，包括成功和错误情况
     * 3. 移除模拟数据逻辑
     *
     * 实现示例：
     * try {
     *     val response = apiService.getTaskDetail(taskId).execute()
     *     if (response.isSuccessful && response.body() != null) {
     *         onSuccess(response.body()!!)
     *     } else {
     *         onError("获取任务详情失败: ${response.code()}")
     *     }
     * } catch (e: Exception) {
     *     onError("网络错误: ${e.message}")
     * }
     */
    private fun getTaskDetailFromServer(
        taskId: String,
        onSuccess: (Task) -> Unit,
        onError: (String) -> Unit
    ) {
        Thread {
            try {
                // TODO: 当后端API准备好后，取消注释以下代码
                /*
                val response = apiService.getTaskDetail(taskId).execute()
                if (response.isSuccessful && response.body() != null) {
                    onSuccess(response.body()!!)
                } else {
                    onError("获取任务详情失败: ${response.code()}")
                }
                */

                // 临时使用模拟数据 - 后端API完成后删除
                val task = mockTasks.find { it.id == taskId }
                if (task != null) {
                    onSuccess(task)
                } else {
                    onError("任务不存在")
                }
            } catch (e: Exception) {
                onError("网络错误: ${e.message}")
            }
        }.start()
    }

    /**
     * 功能：接单操作
     * 后端接入步骤：
     * 1. 取消注释apiService.acceptTask()调用
     * 2. 处理网络响应
     * 3. 移除模拟成功逻辑
     * 调用位置：TaskDetailScreen, 用户点击接单按钮时
     */
    fun acceptTask(
        taskId: String,
        runnerId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        Thread {
            Thread.sleep(800)

            // TODO: 当后端API准备好后，替换为真实网络请求
            /*
            try {
                val response = apiService.acceptTask(taskId).execute()
                if (response.isSuccessful && response.body()?.code == 200) {
                    onSuccess()
                } else {
                    onError("接单失败: ${response.body()?.message ?: "未知错误"}")
                }
            } catch (e: Exception) {
                onError("网络错误: ${e.message}")
            }
            */

            // 模拟随机成功/失败 - 后端API完成后删除
            if (Math.random() > 0.2) {
                onSuccess()
            } else {
                onError("接单失败，请重试")
            }
        }.start()
    }


    /**
     * 功能：发布新任务到服务器
     * 后端接入步骤：
     * 1. 取消注释apiService.createTask()调用
     * 2. 处理网络响应
     * 3. 移除模拟成功逻辑
     * 调用位置：PostScreen, 用户发布任务时
     */
    fun createTask(
        taskRequest: TaskRequest,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        Thread {
            Thread.sleep(1000)

            // TODO: 当后端API准备好后，取消注释以下代码
            /*
            try {
                val response = apiService.createTask(taskRequest).execute()
                if (response.isSuccessful && response.body()?.code == 200) {
                    onSuccess(response.body()?.data ?: "发布成功")
                } else {
                    onError("发布失败: ${response.body()?.message ?: "未知错误"}")
                }
            } catch (e: Exception) {
                onError("网络错误: ${e.message}")
            }
            */

            // 临时模拟成功 - 后端API完成后删除
            onSuccess("任务发布成功！")
        }.start()
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
     */
    fun getTasksFromServer(
        page: Int = 1,
        limit: Int = 20,
        type: String? = null,
        location: String? = null,
        onSuccess: (List<Task>) -> Unit,
        onError: (String) -> Unit
    ) {
        Thread {
            Thread.sleep(500)

            // TODO: 当后端API准备好后，取消注释以下代码
            // try {
            //     val response = apiService.getTasks(page, limit, type, location).execute()
            //     if (response.isSuccessful) {
            //         onSuccess(response.body() ?: emptyList())
            //     } else {
            //         onError("获取任务列表失败: ${response.code()}")
            //     }
            // } catch (e: Exception) {
            //     onError("网络错误: ${e.message}")
            // }

            // 临时使用模拟数据
            onSuccess(mockTasks)
        }.start()
    }
}