package com.example.pratilipi_assignment.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.example.pratilipi_assignment.domain.model.Task
import com.example.pratilipi_assignment.domain.usecase.AddTaskUseCase
import com.example.pratilipi_assignment.domain.usecase.DeleteTaskUseCase
import com.example.pratilipi_assignment.domain.usecase.GetTasksUseCase
import com.example.pratilipi_assignment.domain.usecase.ReorderTasksUseCase
import com.example.pratilipi_assignment.domain.usecase.UpdateTaskUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    val tasks: Flow<PagingData<Task>> = getTasksUseCase()

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
fun reorderTasks(tasks: MutableList<Task?>) {
    viewModelScope.launch {
        tasks.forEachIndexed { index, task ->
            task?.let {
                Log.d("TaskReorder", "Updating task ${it.id} to position $index")
                reorderTasksUseCase(it.copy(position = index))
            }
        }
        refreshList()
    }
}

    private fun refreshList() {
        _refreshTrigger.value += 1
    }
}
