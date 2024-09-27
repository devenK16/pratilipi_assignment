package com.example.pratilipi_assignment.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map as pagingMap
import com.example.pratilipi_assignment.data.local.TaskDao
import com.example.pratilipi_assignment.domain.model.Task
import com.example.pratilipi_assignment.data.model.Task as DataTask
import com.example.pratilipi_assignment.domain.model.Task as DomainTask
import com.example.pratilipi_assignment.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao
) : TaskRepository {

    override fun getAllTasks(): Flow<List<DomainTask>> {
        return taskDao.getAllTasks().map { list ->
            list.map { it.toDomainTask() }
        }
    }
    override fun getPagedTasks(limit: Int, offset: Int): Flow<List<DomainTask>> {
        return taskDao.getPagedTasks(limit, offset).map { list ->
            list.map { it.toDomainTask() }
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
        taskDao.insertTask(task.toDataTask())
    }

    override suspend fun updateTask(task: DomainTask) {
        taskDao.updateTask(task.toDataTask())
    }

    override suspend fun deleteTask(task: DomainTask) {
        taskDao.deleteTask(task.toDataTask())
    }

    override suspend fun getMaxPosition(): Int {
        return taskDao.getMaxPosition() ?: 0
    }

    override suspend fun updateTaskPosition(taskId: Int, newPosition: Int) {
        taskDao.updateTaskPosition(taskId, newPosition)
    }

    private fun DomainTask.toDataTask(): DataTask {
        return DataTask(
            id = id,
            title = title,
            subtitle = subtitle,
            isCompleted = isCompleted,
            position = position
        )
    }
}