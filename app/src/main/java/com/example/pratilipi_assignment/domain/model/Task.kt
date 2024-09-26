package com.example.pratilipi_assignment.domain.model

data class Task(
    val id: Int = 0,
    val title: String,
    val subtitle: String,
    val isCompleted: Boolean = false,
    var position: Int
)