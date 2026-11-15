package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FreeFlowDao {
    @Query("SELECT * FROM drafts ORDER BY updatedAt DESC")
    fun observeDrafts(): Flow<List<DraftEntity>>

    @Query("SELECT * FROM drafts WHERE id = :id LIMIT 1")
    fun observeDraftById(id: Long): Flow<DraftEntity?>

    @Query("SELECT * FROM drafts WHERE id = :id LIMIT 1")
    suspend fun getDraftById(id: Long): DraftEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDraft(draft: DraftEntity): Long

    @Query("DELETE FROM drafts WHERE id = :id")
    suspend fun deleteDraftById(id: Long)

    @Query("UPDATE drafts SET syncStatus = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateDraftSyncStatus(id: Long, status: String, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT * FROM publish_tasks ORDER BY createdAt DESC")
    fun observePublishTasks(): Flow<List<PublishTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPublishTask(task: PublishTaskEntity): Long

    @Query("UPDATE publish_tasks SET status = :status, responseMessage = :message WHERE id = :id")
    suspend fun updateTaskStatus(id: Long, status: String, message: String)

    @Query("DELETE FROM publish_tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)
}
