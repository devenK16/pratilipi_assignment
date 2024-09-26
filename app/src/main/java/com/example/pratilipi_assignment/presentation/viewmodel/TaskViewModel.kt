package com.example.pratilipi_assignment.presentation.viewmodel

import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.example.pratilipi_assignment.domain.model.Task
import com.example.pratilipi_assignment.domain.usecase.AddTaskUseCase
import com.example.pratilipi_assignment.domain.usecase.DeleteTaskUseCase
import com.example.pratilipi_assignment.domain.usecase.GetPagedTasksUseCase
import com.example.pratilipi_assignment.domain.usecase.GetTasksUseCase
import com.example.pratilipi_assignment.domain.usecase.ReorderTasksUseCase
import com.example.pratilipi_assignment.domain.usecase.UpdateTaskUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val getPagedTasksUseCase: GetPagedTasksUseCase,
    private val getTasksUseCase: GetTasksUseCase,
    private val addTaskUseCase: AddTaskUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val reorderTasksUseCase: ReorderTasksUseCase
) : ViewModel() {

    private val _refreshTrigger = MutableStateFlow(0)
    val refreshTrigger: StateFlow<Int> = _refreshTrigger.asStateFlow()

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

//    var tasks: StateFlow<List<Task>> = getPagedTasksUseCase(10, 1 * 10)
//        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _showDialog = MutableStateFlow(false)
    val showDialog: StateFlow<Boolean> = _showDialog.asStateFlow()

    private val _taskToEdit = MutableStateFlow<Task?>(null)
    val taskToEdit: StateFlow<Task?> = _taskToEdit.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val pageSize = 10
    private var currentPage = 0
    var isLastPage = false

    init {
        loadNextPage()
    }

    fun loadNextPage() {
        if (isLastPage || _isLoading.value) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val newTasks = getPagedTasksUseCase(pageSize, currentPage * pageSize).first()
                if (newTasks.isEmpty()) {
                    isLastPage = true
                } else {
                    // Filter out any tasks that are already in the list
                    val uniqueNewTasks = newTasks.filter { newTask ->
                        !_tasks.value.any { it.id == newTask.id }
                    }
                    _tasks.value = _tasks.value + uniqueNewTasks
                    currentPage++
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun openAddTaskDialog() {
        _taskToEdit.value = null
        _showDialog.value = true
    }

    fun openEditTaskDialog(task: Task) {
        _taskToEdit.value = task
        _showDialog.value = true
    }

    fun closeDialog() {
        _showDialog.value = false
        _taskToEdit.value = null
    }

    fun addTask(title: String, subtitle: String) {
        viewModelScope.launch {
            addTaskUseCase(title, subtitle)
            closeDialog()
            // Refresh the entire list to include the new task
            loadNextPage()
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            updateTaskUseCase(task)
            val updatedList = _tasks.value.map { if (it.id == task.id) task else it }
            _tasks.value = updatedList
            closeDialog()
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            deleteTaskUseCase(task)
            _tasks.value = _tasks.value.filter { it.id != task.id }
            closeDialog()
            // Refresh the list to ensure correct ordering
            loadNextPage()
        }
    }

    //    fun reorderTasks(tasks: MutableList<Task?>) {
//        viewModelScope.launch {
//            reorderTasksUseCase(tasks)
//        }
//    }
    fun reorderTasks(tasks: List<Task>) {
        viewModelScope.launch {
            reorderTasksUseCase(tasks) // Pass the entire list, not a single task
            refreshList()
        }
    }

    private fun refreshList() {
        _refreshTrigger.value += 1
    }

    // Move and reorder tasks in the current list
    fun moveTask(fromPosition: Int, toPosition: Int) {
        viewModelScope.launch {
            val currentList = _tasks.value.toMutableList()

            // Move the task in the list
            val movedTask = currentList.removeAt(fromPosition)
            currentList.add(toPosition, movedTask)

            // Update the position in the list (for UI)
            currentList.forEachIndexed { index, task ->
                task.position = index
            }

            _tasks.value = currentList
            // Update the database with new positions
            reorderTasksUseCase(currentList)
        }
    }
}
