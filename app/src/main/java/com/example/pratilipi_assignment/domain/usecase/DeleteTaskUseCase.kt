package com.example.pratilipi_assignment.domain.usecase

import com.example.pratilipi_assignment.domain.model.Task
import com.example.pratilipi_assignment.domain.repository.TaskRepository
import javax.inject.Inject

class DeleteTaskUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(task: Task) {
        repository.deleteTask(task)
    }
}