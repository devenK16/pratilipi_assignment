package com.example.pratilipi_assignment.domain.usecase

import com.example.pratilipi_assignment.domain.model.Task
import com.example.pratilipi_assignment.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPagedTasksUseCase @Inject constructor(private val repository: TaskRepository) {
    operator fun invoke(limit: Int, offset: Int): Flow<List<Task>> =
        repository.getPagedTasks(limit, offset)
}