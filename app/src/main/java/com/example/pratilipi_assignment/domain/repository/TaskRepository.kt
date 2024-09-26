package com.example.pratilipi_assignment.domain.repository

import androidx.paging.PagingData
import androidx.paging.PagingSource
import com.example.pratilipi_assignment.domain.model.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
//    fun getAllTasksPaged(): Flow<PagingData<Task>>
    fun getAllTasks(): Flow<List<Task>>
    suspend fun insertTask(task: Task)
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(task: Task)
    suspend fun getMaxPosition(): Int
    fun getPagedTasks(limit: Int, offset: Int): Flow<List<Task>>

    // NEW
    suspend fun updateTaskPosition(taskId: Int, newPosition: Int)
}