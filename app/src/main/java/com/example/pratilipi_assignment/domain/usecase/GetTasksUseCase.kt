package com.example.pratilipi_assignment.domain.usecase

import androidx.paging.PagingData
import com.example.pratilipi_assignment.domain.model.Task
import com.example.pratilipi_assignment.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTasksUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    operator fun invoke(): Flow<PagingData<Task>> = repository.getAllTasksPaged()
}