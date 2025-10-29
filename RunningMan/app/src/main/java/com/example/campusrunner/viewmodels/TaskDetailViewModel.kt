package com.example.campusrunner.viewmodels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusrunner.data.TaskRepository
import com.example.campusrunner.model.Task
import kotlinx.coroutines.launch

/**
 * 任务详情ViewModel - 管理任务详情页面的状态和业务逻辑
 */
class TaskDetailViewModel : ViewModel() {

    // 任务详情状态
    private val _taskState = mutableStateOf<Task?>(null)
    val taskState: State<Task?> = _taskState

    // 加载状态
    private val _loadingState = mutableStateOf(false)
    val loadingState: State<Boolean> = _loadingState

    // 错误状态
    private val _errorState = mutableStateOf<String?>(null)
    val errorState: State<String?> = _errorState

    // 接单状态
    private val _acceptTaskState = mutableStateOf<AcceptTaskState>(AcceptTaskState.Idle)
    val acceptTaskState: State<AcceptTaskState> = _acceptTaskState

    /**
     * 加载任务详情
     * @param taskId 任务ID
     */
    fun loadTaskDetail(taskId: String) {
        _loadingState.value = true
        _errorState.value = null

        viewModelScope.launch {
            try {
                val result = TaskRepository.getTaskById(taskId)

                result.onSuccess { task ->
                    _taskState.value = task

                    // 订阅任务状态更新
                    TaskRepository.subscribeToTaskUpdates(taskId) { newStatus ->
                        _taskState.value = _taskState.value?.copy(status = newStatus)
                    }
                }.onFailure { error ->
                    _errorState.value = error.message ?: "加载任务详情失败"
                }

            } catch (e: Exception) {
                _errorState.value = "加载异常: ${e.message}"
            } finally {
                _loadingState.value = false
            }
        }
    }

    /**
     * 接单操作
     * @param taskId 任务ID
     */
    fun acceptTask(taskId: String) {
        _acceptTaskState.value = AcceptTaskState.Loading

        viewModelScope.launch {
            try {
                // runnerId 已移除，假设后端通过 token 自动识别用户
                val result = TaskRepository.acceptTask(taskId)

                result.onSuccess {
                    _acceptTaskState.value = AcceptTaskState.Success
                    // 重新加载任务详情以更新状态
                    loadTaskDetail(taskId)
                }.onFailure { error ->
                    _acceptTaskState.value = AcceptTaskState.Error(error.message ?: "接单失败")
                }
            } catch (e: Exception) {
                _acceptTaskState.value = AcceptTaskState.Error("接单异常: ${e.message}")
            }
        }
    }

    /**
     * 清除错误状态
     */
    fun clearError() {
        _errorState.value = null
    }

    /**
     * 清除接单状态
     */
    fun clearAcceptState() {
        _acceptTaskState.value = AcceptTaskState.Idle
    }
}

/**
 * 接单状态密封类
 */
sealed class AcceptTaskState {
    object Idle : AcceptTaskState()
    object Loading : AcceptTaskState()
    object Success : AcceptTaskState()
    data class Error(val message: String) : AcceptTaskState()
}
