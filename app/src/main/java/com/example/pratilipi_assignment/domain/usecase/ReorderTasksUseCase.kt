package com.example.pratilipi_assignment.domain.usecase

import com.example.pratilipi_assignment.domain.model.Task
import com.example.pratilipi_assignment.domain.repository.TaskRepository
import javax.inject.Inject


class ReorderTasksUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(tasks: List<Task>) {
        tasks.forEach { task ->
            repository.updateTaskPosition(task.id, task.position)
        }
    }
}