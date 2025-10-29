package com.example.campusrunner.viewmodels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusrunner.data.SearchRepository
import com.example.campusrunner.data.TaskRepository
import com.example.campusrunner.model.SearchHistory
import com.example.campusrunner.model.Task
import kotlinx.coroutines.launch

/**
 * 首页ViewModel - 管理搜索状态和任务数据
 */
class HomeViewModel : ViewModel() {

    // 搜索状态
    private val _searchState = mutableStateOf(false)
    val searchState: State<Boolean> = _searchState

    // 搜索文本
    private val _searchText = mutableStateOf("")
    val searchText: State<String> = _searchText

    // 搜索历史
    private val _searchHistory = mutableStateOf<List<SearchHistory>>(emptyList())
    val searchHistory: State<List<SearchHistory>> = _searchHistory

    // 任务列表
    private val _tasks = mutableStateOf<List<Task>>(emptyList())
    val tasks: State<List<Task>> = _tasks

    // 加载状态
    private val _loadingState = mutableStateOf(false)
    val loadingState: State<Boolean> = _loadingState

    // 错误状态
    private val _errorState = mutableStateOf<String?>(null)
    val errorState: State<String?> = _errorState

    // 当前用户ID（模拟数据）
    // TODO: 实际接入时，应从登录态或本地存储中获取真实用户ID
    private val currentUserId = "current_user"

    init {
        loadTasks()
        loadSearchHistory()
    }

    /**
     * 打开搜索界面
     */
    fun openSearch() {
        _searchState.value = true
        loadSearchHistory()
    }

    /**
     * 关闭搜索界面
     */
    fun closeSearch() {
        _searchState.value = false
        _searchText.value = ""
    }

    /**
     * 更新搜索文本
     */
    fun updateSearchText(text: String) {
        _searchText.value = text
    }

    /**
     * 执行搜索
     */
    fun performSearch(keyword: String) {
        if (keyword.isBlank()) return

        _loadingState.value = true
        _errorState.value = null

        viewModelScope.launch {
            try {
                // TODO: 这里的 SearchRepository 也需要按照 TaskRepository 的模式进行修改
                // 1. 将 SearchRepository.searchTasks 改为 suspend fun ... : Result<List<Task>>
                // 2. 移除 onSuccess / onError 回调
                // 3. 在 viewModelScope.launch 中处理 Result

                // 模拟搜索延迟
                kotlinx.coroutines.delay(500)

                SearchRepository.searchTasks(
                    keyword = keyword,
                    onSuccess = { tasks ->
                        _loadingState.value = false
                        _tasks.value = tasks
                        closeSearch() // 搜索完成后关闭搜索界面
                    },
                    onError = { error ->
                        _loadingState.value = false
                        _errorState.value = "搜索失败: $error"
                    }
                )
            } catch (e: Exception) {
                _loadingState.value = false
                _errorState.value = "搜索异常: ${e.message}"
            }
        }
    }

    /**
     * 加载搜索历史
     */
    fun loadSearchHistory() {
        viewModelScope.launch {
            try {
                // TODO: 这里的 SearchRepository 也需要按照 TaskRepository 的模式进行修改
                // 1. 将 SearchRepository.getSearchHistory 改为 suspend fun ... : Result<List<SearchHistory>>
                // 2. 移除 onSuccess / onError 回调
                SearchRepository.getSearchHistory(
                    userId = currentUserId,
                    limit = 10,
                    onSuccess = { histories ->
                        _searchHistory.value = histories
                    },
                    onError = { error ->
                        _errorState.value = "加载搜索历史失败: $error"
                    }
                )
            } catch (e: Exception) {
                _errorState.value = "加载搜索历史异常: ${e.message}"
            }
        }
    }

    /**
     * 删除搜索历史项
     */
    fun deleteSearchHistory(historyId: String) {
        viewModelScope.launch {
            try {
                // TODO: 这里的 SearchRepository 也需要按照 TaskRepository 的模式进行修改
                SearchRepository.deleteSearchHistory(
                    historyId = historyId,
                    onSuccess = {
                        loadSearchHistory() // 重新加载
                    },
                    onError = { error ->
                        _errorState.value = "删除搜索历史失败: $error"
                    }
                )
            } catch (e: Exception) {
                _errorState.value = "删除搜索历史异常: ${e.message}"
            }
        }
    }

    /**
     * 清空搜索历史
     */
    fun clearSearchHistory() {
        viewModelScope.launch {
            try {
                // TODO: 这里的 SearchRepository 也需要按照 TaskRepository 的模式进行修改
                SearchRepository.clearSearchHistory(
                    userId = currentUserId,
                    onSuccess = {
                        _searchHistory.value = emptyList()
                    },
                    onError = { error ->
                        _errorState.value = "清空搜索历史失败: $error"
                    }
                )
            } catch (e: Exception) {
                _errorState.value = "清空搜索历史异常: ${e.message}"
            }
        }
    }

    /**
     * 加载任务列表
     * 已更新为调用 suspend 函数并处理 Result
     */
    fun loadTasks() {
        _loadingState.value = true
        _errorState.value = null // 开始加载时清除之前的错误信息

        viewModelScope.launch {
            try {
                // 调用重构后的 suspend 函数
                val result = TaskRepository.getTasksFromServer()

                // 使用 Result 的 onSucess 和 onFailure 处理结果
                result.onSuccess { tasks ->
                    _tasks.value = tasks
                }.onFailure { error ->
                    _errorState.value = "加载任务失败: ${error.message}"
                }

            } catch (e: Exception) {
                // 捕获协程中的意外异常 (例如 kotlinx.coroutines.JobCancellationException)
                _errorState.value = "加载任务异常: ${e.message}"
            } finally {
                // 无论成功还是失败，最后都要停止加载状态
                _loadingState.value = false
            }
        }
    }

    /**
     * 清除错误状态
     */
    fun clearError() {
        _errorState.value = null
    }
}

