package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class DraftContentType(val label: String) {
    ARTICLE("长图文"),
    SHORT_NOTE("短动态"),
    VIDEO_SCRIPT("视频脚本")
}

enum class DraftSyncStatus(val label: String) {
    LOCAL_ONLY("仅本地草稿"),
    QUEUED("待桌面端分发"),
    SYNCED("已同步桌面端"),
    FAILED("分发失败")
}

@Entity(tableName = "drafts")
data class DraftEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val contentMarkdown: String,
    val contentType: String = DraftContentType.ARTICLE.name,
    val tagsCsv: String = "",
    val coverUri: String = "",
    val mediaUrisCsv: String = "",
    val selectedPlatformIdsCsv: String = "wechat_mp,zhihu,juejin",
    val syncStatus: String = DraftSyncStatus.LOCAL_ONLY.name,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun platformIds(): List<String> =
        selectedPlatformIdsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }

    fun mediaUris(): List<String> =
        mediaUrisCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }

    fun tags(): List<String> =
        tagsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }

    fun resolvedContentType(): DraftContentType =
        DraftContentType.entries.find { it.name == contentType } ?: DraftContentType.ARTICLE

    fun resolvedSyncStatus(): DraftSyncStatus =
        DraftSyncStatus.entries.find { it.name == syncStatus } ?: DraftSyncStatus.LOCAL_ONLY
}
