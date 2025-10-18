package com.example.campusrunner.ui.viewmodels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusrunner.data.TaskRepository
import com.example.campusrunner.model.Task
import com.example.campusrunner.model.TaskStatus
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

        TaskRepository.getTaskById(
            taskId = taskId,
            onSuccess = { task ->
                _loadingState.value = false
                _taskState.value = task

                // 订阅任务状态更新
                TaskRepository.subscribeToTaskUpdates(taskId) { newStatus ->
                    _taskState.value = _taskState.value?.copy(status = newStatus)
                }
            },
            onError = { error ->
                _loadingState.value = false
                _errorState.value = error
            }
        )
    }

    /**
     * 接单操作
     * @param taskId 任务ID
     * @param runnerId 跑腿员ID（在实际应用中从用户信息获取）
     */
    fun acceptTask(taskId: String, runnerId: String = "current_user_id") {
        _acceptTaskState.value = AcceptTaskState.Loading

        TaskRepository.acceptTask(
            taskId = taskId,
            runnerId = runnerId,
            onSuccess = {
                _acceptTaskState.value = AcceptTaskState.Success
                // 重新加载任务详情以更新状态
                loadTaskDetail(taskId)
            },
            onError = { error ->
                _acceptTaskState.value = AcceptTaskState.Error(error)
            }
        )
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