package com.example.pratilipi_assignment.di

import android.app.Application
import androidx.room.Room
import com.example.pratilipi_assignment.data.local.TaskDao
import com.example.pratilipi_assignment.data.local.TaskDatabase
import com.example.pratilipi_assignment.data.repository.TaskRepositoryImpl
import com.example.pratilipi_assignment.domain.repository.TaskRepository
import com.example.pratilipi_assignment.domain.usecase.AddTaskUseCase
import com.example.pratilipi_assignment.domain.usecase.DeleteTaskUseCase
import com.example.pratilipi_assignment.domain.usecase.GetTasksUseCase
import com.example.pratilipi_assignment.domain.usecase.ReorderTasksUseCase
import com.example.pratilipi_assignment.domain.usecase.UpdateTaskUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideTaskDatabase(app: Application): TaskDatabase {
        return Room.databaseBuilder(
            app,
            TaskDatabase::class.java,
            "task_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideTaskDao(database: TaskDatabase): TaskDao {
        return database.taskDao()
    }

    @Provides
    @Singleton
    fun provideTaskRepository(taskDao: TaskDao): TaskRepository {
        return TaskRepositoryImpl(taskDao)
    }

    @Provides
    @Singleton
    fun provideGetTasksUseCase(repository: TaskRepository): GetTasksUseCase {
        return GetTasksUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideAddTaskUseCase(repository: TaskRepository): AddTaskUseCase {
        return AddTaskUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideUpdateTaskUseCase(repository: TaskRepository): UpdateTaskUseCase {
        return UpdateTaskUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideDeleteTaskUseCase(repository: TaskRepository): DeleteTaskUseCase {
        return DeleteTaskUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideReorderTasksUseCase(repository: TaskRepository): ReorderTasksUseCase {
        return ReorderTasksUseCase(repository)
    }
}