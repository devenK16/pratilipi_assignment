package com.example.pratilipi_assignment.domain.usecase

import com.example.pratilipi_assignment.domain.model.Task
import com.example.pratilipi_assignment.domain.repository.TaskRepository
import javax.inject.Inject

class AddTaskUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(title: String, subtitle: String) {
        val maxPosition = repository.getMaxPosition()
        val task = Task(
            title = title,
            subtitle = subtitle,
            isCompleted = false,
            position = maxPosition + 1
        )
        repository.insertTask(task)
    }
}