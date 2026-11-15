package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TaskDispatchStatus(val label: String) {
    PENDING("队列中"),
    DISPATCHED("已送达桌面端"),
    FAILED("推送失败")
}

@Entity(tableName = "publish_tasks")
data class PublishTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val draftId: Long,
    val draftTitle: String,
    val targetPlatformsCsv: String,
    val status: String = TaskDispatchStatus.PENDING.name,
    val desktopEndpoint: String,
    val responseMessage: String,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun resolvedStatus(): TaskDispatchStatus =
        TaskDispatchStatus.entries.find { it.name == status } ?: TaskDispatchStatus.PENDING
}
