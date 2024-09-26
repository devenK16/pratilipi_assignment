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
    private val getTasksUseCase: GetTasksUseCase,
    private val addTaskUseCase: AddTaskUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val reorderTasksUseCase: ReorderTasksUseCase
) : ViewModel() {

    private val _refreshTrigger = MutableStateFlow(0)
    val refreshTrigger: StateFlow<Int> = _refreshTrigger.asStateFlow()

    val tasks: StateFlow<List<Task>> = getTasksUseCase()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _showDialog = MutableStateFlow(false)
    val showDialog: StateFlow<Boolean> = _showDialog.asStateFlow()

    private val _taskToEdit = MutableStateFlow<Task?>(null)
    val taskToEdit: StateFlow<Task?> = _taskToEdit.asStateFlow()

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
            // NEW
            refreshList()
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            updateTaskUseCase(task)
            closeDialog()
            // NEW
            refreshList()
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            deleteTaskUseCase(task)
            closeDialog()
            // NEW
            refreshList()
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
            val currentList = tasks.value.toMutableList()

            // Move the task in the list
            val movedTask = currentList.removeAt(fromPosition)
            currentList.add(toPosition, movedTask)

            // Update the position in the list (for UI)
            currentList.forEachIndexed { index, task ->
                task.position = index
            }

            // Update the database with new positions
            reorderTasksUseCase(currentList)
        }
    }
}
