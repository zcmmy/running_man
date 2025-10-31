package com.example.campusrunner.viewmodels

import android.util.Log
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
        // 错误修复：不在初始化时加载搜索历史，
        // 仅在用户打开搜索栏时 (openSearch) 才加载。
        // loadSearchHistory()
    }

    /**
     * 打开搜索界面
     */
    fun openSearch() {
        _searchState.value = true
        // 在这里加载搜索历史是正确的
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
     * (已适配 SearchRepository)
     * [已修复] 修正了在非协程作用域中调用 suspend 函数的问题
     */
    fun performSearch(keyword: String) {
        if (keyword.isBlank()) return

        _loadingState.value = true
        _errorState.value = null

        viewModelScope.launch {
            try {
                // 1. 执行搜索, 如果失败则抛出异常
                val tasks = SearchRepository.searchTasks(keyword).getOrThrow()

                // 搜索成功
                _tasks.value = tasks
                // closeSearch() // !! 移除 !! - 我们不再关闭搜索，而是导航到结果页

                // 2. 搜索成功后，将关键字添加到搜索历史
                // 启动一个*新*的协程来处理这个非关键的后台任务
                // 这样它就不会阻塞UI，并且可以安全地调用 suspend 函数
                viewModelScope.launch {
                    try {
                        SearchRepository.addSearchHistory(currentUserId, keyword)
                    } catch (e: Exception) {
                        // 忽略添加历史的错误，只在控制台打印
                        println("添加搜索历史失败: ${e.message}")
                    }
                }

            } catch (e: Exception) {
                // 捕获 searchTasks.getOrThrow() 抛出的异常
                _errorState.value = "搜索异常: ${e.message}"
            } finally {
                _loadingState.value = false
            }
        }
    }

    /**
     * 加载搜索历史
     * (已适配 SearchRepository)
     */
    fun loadSearchHistory() {
        viewModelScope.launch {
            try {
                val result = SearchRepository.getSearchHistory(
                    userId = currentUserId,
                    limit = 10
                )
                result.onSuccess { histories ->
                    _searchHistory.value = histories
                }.onFailure { error ->
                    // 仅在搜索历史加载失败时显示错误，不影响首页任务列表
                    _errorState.value = "加载搜索历史失败: ${error.message}"
                }
            } catch (e: Exception) {
                _errorState.value = "加载搜索历史异常: ${e.message}"
            }
        }
    }

    /**
     * 删除搜索历史项
     * (已适配 SearchRepository)
     */
    fun deleteSearchHistory(historyId: String) {
        viewModelScope.launch {
            try {
                val result = SearchRepository.deleteSearchHistory(historyId)

                result.onSuccess {
                    loadSearchHistory() // 重新加载
                }.onFailure { error ->
                    _errorState.value = "删除搜索历史失败: ${error.message}"
                }
            } catch (e: Exception) {
                _errorState.value = "删除搜索历史异常: ${e.message}"
            }
        }
    }

    /**
     * 清空搜索历史
     * (已适配 SearchRepository)
     */
    fun clearSearchHistory() {
        viewModelScope.launch {
            try {
                val result = SearchRepository.clearSearchHistory(userId = currentUserId)
                result.onSuccess {
                    _searchHistory.value = emptyList() // 立即清空UI
                }.onFailure { error ->
                    _errorState.value = "清空搜索历史失败: ${error.message}"
                }
            } catch (e: Exception) {
                _errorState.value = "清空搜索历史异常: ${e.message}"
            }
        }
    }

    /**
     * 加载任务列表
     * (已适配 TaskRepository)
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
                // 捕获协程中的意外异常
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

