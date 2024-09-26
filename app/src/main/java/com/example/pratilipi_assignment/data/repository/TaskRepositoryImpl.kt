package com.example.pratilipi_assignment.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map as pagingMap
import com.example.pratilipi_assignment.data.local.TaskDao
import com.example.pratilipi_assignment.data.model.Task as DataTask
import com.example.pratilipi_assignment.domain.model.Task as DomainTask
import com.example.pratilipi_assignment.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao
) : TaskRepository {

    override fun getAllTasksPaged(): Flow<PagingData<DomainTask>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { taskDao.getAllTasksPaged() }
        ).flow.map { pagingData ->
            pagingData.pagingMap { it.toDomainTask() }
        }
    }

    private fun DataTask.toDomainTask(): DomainTask {
        return DomainTask(
            id = id,
            title = title,
            subtitle = subtitle,
            isCompleted = isCompleted,
            position = position
        )
    }

    override suspend fun insertTask(task: DomainTask) {
        taskDao.insertTask(
            DataTask(
                title = task.title,
                subtitle = task.subtitle,
                isCompleted = task.isCompleted,
                position = task.position
            )
        )
    }

    override suspend fun updateTask(task: DomainTask) {
        taskDao.updateTask(
            DataTask(
                id = task.id,
                title = task.title,
                subtitle = task.subtitle,
                isCompleted = task.isCompleted,
                position = task.position
            )
        )
    }

    override suspend fun deleteTask(task: DomainTask) {
        taskDao.deleteTask(
            DataTask(
                id = task.id,
                title = task.title,
                subtitle = task.subtitle,
                isCompleted = task.isCompleted,
                position = task.position
            )
        )
    }

    override suspend fun getMaxPosition(): Int {
        return taskDao.getMaxPosition() ?: 0
    }

    // NEW
     override suspend fun updateTaskPosition(taskId: Int, newPosition: Int) {
        taskDao.updateTaskPosition(taskId, newPosition)
    }
}