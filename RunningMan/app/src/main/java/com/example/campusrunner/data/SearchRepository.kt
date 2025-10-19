package com.example.campusrunner.data

import com.example.campusrunner.model.SearchHistory
import com.example.campusrunner.model.SearchHistoryRequest
import com.example.campusrunner.model.SearchHistoryResponse
import com.example.campusrunner.model.Task
import com.example.campusrunner.network.ApiService
import com.example.campusrunner.network.RetrofitClient
import java.util.*

/**
 * 搜索数据仓库
 */
object SearchRepository {

    private val apiService: ApiService = RetrofitClient.apiService

    // 模拟搜索历史数据（开发阶段使用）
    private val mockSearchHistory = listOf(
        SearchHistory(
            id = "1",
            userId = "current_user",
            keyword = "麦当劳",
            searchCount = 5,
            lastSearchedAt = Date(System.currentTimeMillis() - 10 * 60 * 1000), // 10分钟前
            createdAt = Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000) // 7天前
        ),
        SearchHistory(
            id = "2",
            userId = "current_user",
            keyword = "打印",
            searchCount = 3,
            lastSearchedAt = Date(System.currentTimeMillis() - 30 * 60 * 1000), // 30分钟前
            createdAt = Date(System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000) // 3天前
        ),
        SearchHistory(
            id = "3",
            userId = "current_user",
            keyword = "快递",
            searchCount = 8,
            lastSearchedAt = Date(System.currentTimeMillis() - 2 * 60 * 60 * 1000), // 2小时前
            createdAt = Date(System.currentTimeMillis() - 14 * 24 * 60 * 60 * 1000) // 14天前
        ),
        SearchHistory(
            id = "4",
            userId = "current_user",
            keyword = "奶茶",
            searchCount = 2,
            lastSearchedAt = Date(System.currentTimeMillis() - 1 * 24 * 60 * 60 * 1000), // 1天前
            createdAt = Date(System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000) // 2天前
        )
    )

    /**
     * 获取搜索历史
     */
    fun getSearchHistory(
        userId: String,
        limit: Int = 10,
        onSuccess: (List<SearchHistory>) -> Unit,
        onError: (String) -> Unit
    ) {
        Thread {
            Thread.sleep(300)

            // TODO: 当后端API准备好后，取消注释以下代码
            // try {
            //     val response = apiService.getSearchHistory(userId, limit).execute()
            //     if (response.isSuccessful && response.body() != null) {
            //         onSuccess(response.body()!!.histories)
            //     } else {
            //         onError("获取搜索历史失败: ${response.code()}")
            //     }
            // } catch (e: Exception) {
            //     onError("网络错误: ${e.message}")
            // }

            // 临时使用模拟数据
            val histories = mockSearchHistory
                .filter { it.userId == userId }
                .sortedByDescending { it.lastSearchedAt }
                .take(limit)
            onSuccess(histories)
        }.start()
    }

    /**
     * 添加搜索历史
     */
    fun addSearchHistory(
        userId: String,
        keyword: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        Thread {
            Thread.sleep(200)

            // TODO: 当后端API准备好后，取消注释以下代码
            // try {
            //     val response = apiService.addSearchHistory(SearchHistoryRequest(keyword, userId)).execute()
            //     if (response.isSuccessful && response.body()?.code == 200) {
            //         onSuccess()
            //     } else {
            //         onError("添加搜索历史失败: ${response.body()?.message ?: "未知错误"}")
            //     }
            // } catch (e: Exception) {
            //     onError("网络错误: ${e.message}")
            // }

            // 临时模拟成功
            onSuccess()
        }.start()
    }

    /**
     * 删除单个搜索历史
     */
    fun deleteSearchHistory(
        historyId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        Thread {
            Thread.sleep(200)

            // TODO: 当后端API准备好后，取消注释以下代码
            // try {
            //     val response = apiService.deleteSearchHistory(historyId).execute()
            //     if (response.isSuccessful && response.body()?.code == 200) {
            //         onSuccess()
            //     } else {
            //         onError("删除搜索历史失败: ${response.body()?.message ?: "未知错误"}")
            //     }
            // } catch (e: Exception) {
            //     onError("网络错误: ${e.message}")
            // }

            // 临时模拟成功
            onSuccess()
        }.start()
    }

    /**
     * 清空搜索历史
     */
    fun clearSearchHistory(
        userId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        Thread {
            Thread.sleep(200)

            // TODO: 当后端API准备好后，取消注释以下代码
            // try {
            //     val response = apiService.clearSearchHistory(userId).execute()
            //     if (response.isSuccessful && response.body()?.code == 200) {
            //         onSuccess()
            //     } else {
            //         onError("清空搜索历史失败: ${response.body()?.message ?: "未知错误"}")
            //     }
            // } catch (e: Exception) {
            //     onError("网络错误: ${e.message}")
            // }

            // 临时模拟成功
            onSuccess()
        }.start()
    }

    /**
     * 搜索任务
     */
    fun searchTasks(
        keyword: String,
        onSuccess: (List<Task>) -> Unit,
        onError: (String) -> Unit
    ) {
        Thread {
            Thread.sleep(500)

            // TODO: 当后端API准备好后，取消注释以下代码
            // try {
            //     val response = apiService.getTasks(search = keyword).execute()
            //     if (response.isSuccessful) {
            //         onSuccess(response.body() ?: emptyList())
            //     } else {
            //         onError("搜索失败: ${response.code()}")
            //     }
            // } catch (e: Exception) {
            //     onError("网络错误: ${e.message}")
            // }

            // 临时使用TaskRepository的模拟数据并过滤
            val allTasks = getAllMockTasksForSearch()
            val filteredTasks = allTasks.filter { task ->
                task.title.contains(keyword, ignoreCase = true) ||
                        task.description.contains(keyword, ignoreCase = true) ||
                        task.location.contains(keyword, ignoreCase = true) ||
                        task.destination.contains(keyword, ignoreCase = true) ||
                        task.getTypeText().contains(keyword, ignoreCase = true)
            }
            onSuccess(filteredTasks)
        }.start()
    }

    /**
     * 获取用于搜索的模拟任务数据
     * 这个方法复制了TaskRepository中的模拟数据，避免循环依赖
     */
    private fun getAllMockTasksForSearch(): List<Task> {
        return listOf(
            Task(
                id = "1",
                title = "麦当劳大套餐",
                description = "校门口取餐，送至图书馆三楼 A301。请确保食物保持温热状态，饮料不要摇晃。",
                price = 15.0,
                type = com.example.campusrunner.model.TaskType.FOOD_DELIVERY,
                status = com.example.campusrunner.model.TaskStatus.PENDING,
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
                type = com.example.campusrunner.model.TaskType.PRINT,
                status = com.example.campusrunner.model.TaskStatus.IN_PROGRESS,
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
                type = com.example.campusrunner.model.TaskType.EXPRESS,
                status = com.example.campusrunner.model.TaskStatus.PENDING,
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
                type = com.example.campusrunner.model.TaskType.SHOPPING,
                status = com.example.campusrunner.model.TaskStatus.PENDING,
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
            ),
            Task(
                id = "5",
                title = "取中通快递",
                description = "取中通快递，收件人：李华",
                price = 6.0,
                type = com.example.campusrunner.model.TaskType.EXPRESS,
                status = com.example.campusrunner.model.TaskStatus.PENDING,
                location = "快递点",
                destination = "宿舍区",
                distance = 0.8,
                publisherId = "user5",
                publisherName = "刘同学",
                publisherAvatar = null,
                publisherRating = 4.6,
                createdAt = Date(System.currentTimeMillis() - 3 * 60 * 1000),
                acceptedAt = null,
                completedAt = null,
                runnerId = null,
                runnerName = null
            ),
            Task(
                id = "6",
                title = "买冰可乐",
                description = "超市买一瓶冰可乐，送到操场",
                price = 4.0,
                type = com.example.campusrunner.model.TaskType.SHOPPING,
                status = com.example.campusrunner.model.TaskStatus.PENDING,
                location = "超市",
                destination = "操场",
                distance = 1.2,
                publisherId = "user6",
                publisherName = "陈同学",
                publisherAvatar = null,
                publisherRating = 4.7,
                createdAt = Date(System.currentTimeMillis() - 8 * 60 * 1000),
                acceptedAt = null,
                completedAt = null,
                runnerId = null,
                runnerName = null
            ),
            Task(
                id = "7",
                title = "打印简历",
                description = "打印店取 5 份简历，送至宿舍",
                price = 10.0,
                type = com.example.campusrunner.model.TaskType.PRINT,
                status = com.example.campusrunner.model.TaskStatus.PENDING,
                location = "打印店",
                destination = "宿舍",
                distance = 0.3,
                publisherId = "user7",
                publisherName = "杨同学",
                publisherAvatar = null,
                publisherRating = 4.9,
                createdAt = Date(System.currentTimeMillis() - 15 * 60 * 1000),
                acceptedAt = null,
                completedAt = null,
                runnerId = null,
                runnerName = null
            ),
            Task(
                id = "8",
                title = "买薯片和可乐",
                description = "小卖部买零食，送到宿舍",
                price = 12.0,
                type = com.example.campusrunner.model.TaskType.SHOPPING,
                status = com.example.campusrunner.model.TaskStatus.IN_PROGRESS,
                location = "小卖部",
                destination = "宿舍",
                distance = 0.1,
                publisherId = "user8",
                publisherName = "黄同学",
                publisherAvatar = null,
                publisherRating = 4.8,
                createdAt = Date(System.currentTimeMillis() - 25 * 60 * 1000),
                acceptedAt = Date(System.currentTimeMillis() - 10 * 60 * 1000),
                completedAt = null,
                runnerId = "runner2",
                runnerName = "赵跑腿"
            )
        )
    }
}