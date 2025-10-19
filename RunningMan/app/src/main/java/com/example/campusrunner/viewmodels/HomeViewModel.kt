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
                // 模拟搜索延迟
                kotlinx.coroutines.delay(500)

                // 添加搜索历史（模拟）
                SearchRepository.addSearchHistory(
                    userId = currentUserId,
                    keyword = keyword,
                    onSuccess = {
                        // 重新加载搜索历史
                        loadSearchHistory()
                    },
                    onError = { error ->
                        _errorState.value = "添加搜索历史失败: $error"
                    }
                )

                // 执行搜索（模拟）
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
     */
    fun loadTasks() {
        _loadingState.value = true

        viewModelScope.launch {
            try {
                // 模拟网络延迟
                kotlinx.coroutines.delay(1000)

                TaskRepository.getTasksFromServer(
                    onSuccess = { tasks ->
                        _loadingState.value = false
                        _tasks.value = tasks
                    },
                    onError = { error ->
                        _loadingState.value = false
                        _errorState.value = "加载任务失败: $error"
                    }
                )
            } catch (e: Exception) {
                _loadingState.value = false
                _errorState.value = "加载任务异常: ${e.message}"
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