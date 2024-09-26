package com.example.pratilipi_assignment.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.pratilipi_assignment.data.model.Task

@Database(entities = [Task::class], version = 2, exportSchema = false)
abstract class TaskDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
}