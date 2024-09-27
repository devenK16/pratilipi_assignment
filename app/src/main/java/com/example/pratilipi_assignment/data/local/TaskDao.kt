package com.example.pratilipi_assignment.data.local

import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.pratilipi_assignment.data.model.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM task_table ORDER BY position ASC")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM task_table ORDER BY position ASC LIMIT :limit OFFSET :offset")
    fun getPagedTasks(limit: Int, offset: Int): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)


    @Query("SELECT MAX(position) FROM task_table")
    suspend fun getMaxPosition(): Int?

    @Query("UPDATE task_table SET position = :newPosition WHERE id = :taskId")
    suspend fun updateTaskPosition(taskId: Int, newPosition: Int)
}