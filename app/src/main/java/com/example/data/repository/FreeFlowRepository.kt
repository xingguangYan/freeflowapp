package com.example.data.repository

import com.example.data.local.BridgePreferences
import com.example.data.local.DraftEntity
import com.example.data.local.DraftSyncStatus
import com.example.data.local.FreeFlowDao
import com.example.data.local.PublishTaskEntity
import com.example.data.local.TaskDispatchStatus
import com.example.data.remote.DispatchResult
import com.example.data.remote.FreeFlowBridgeClient
import com.example.domain.model.BridgeConfig
import com.example.domain.model.ConnectionState
import com.example.domain.model.DesktopAccountStatus
import kotlinx.coroutines.flow.Flow

class FreeFlowRepository(
    private val dao: FreeFlowDao,
    private val preferences: BridgePreferences,
    private val bridgeClient: FreeFlowBridgeClient
) {
    val drafts: Flow<List<DraftEntity>> = dao.observeDrafts()
    val publishTasks: Flow<List<PublishTaskEntity>> = dao.observePublishTasks()
    val bridgeConfig: Flow<BridgeConfig> = preferences.bridgeConfig
    val defaultPlatformIds: Flow<Set<String>> = preferences.defaultPlatformIds

    fun observeDraft(id: Long): Flow<DraftEntity?> = dao.observeDraftById(id)

    suspend fun getDraft(id: Long): DraftEntity? = dao.getDraftById(id)

    suspend fun saveDraft(draft: DraftEntity): Long {
        return dao.upsertDraft(draft.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteDraft(id: Long) {
        dao.deleteDraftById(id)
    }

    suspend fun deleteTask(id: Long) {
        dao.deleteTaskById(id)
    }

    suspend fun saveBridgeConfig(hostAddress: String, pairingToken: String) {
        preferences.saveBridgeConfig(hostAddress, pairingToken)
    }

    suspend fun saveDefaultPlatforms(platformIds: Set<String>) {
        preferences.saveDefaultPlatforms(platformIds)
    }

    suspend fun pingDesktop(config: BridgeConfig): ConnectionState {
        return bridgeClient.pingHealth(config.hostAddress, config.pairingToken)
    }

    suspend fun fetchDesktopAccounts(config: BridgeConfig): Result<List<DesktopAccountStatus>> {
        return bridgeClient.fetchAccounts(config.hostAddress, config.pairingToken)
    }

    suspend fun dispatchPublish(draft: DraftEntity, config: BridgeConfig): DispatchResult {
        val savedId = dao.upsertDraft(
            draft.copy(
                syncStatus = DraftSyncStatus.QUEUED.name,
                updatedAt = System.currentTimeMillis()
            )
        )
        val effectiveDraft = draft.copy(id = savedId)
        val taskId = dao.insertPublishTask(
            PublishTaskEntity(
                draftId = savedId,
                draftTitle = effectiveDraft.title.ifBlank { "未命名稿件" },
                targetPlatformsCsv = effectiveDraft.selectedPlatformIdsCsv,
                status = TaskDispatchStatus.PENDING.name,
                desktopEndpoint = config.hostAddress,
                responseMessage = "正在通过 LAN 发送至桌面端…"
            )
        )

        val result = bridgeClient.dispatchDraftPublish(
            hostAddress = config.hostAddress,
            token = config.pairingToken,
            draft = effectiveDraft
        )

        if (result.success) {
            dao.updateDraftSyncStatus(savedId, DraftSyncStatus.SYNCED.name)
            dao.updateTaskStatus(taskId, TaskDispatchStatus.DISPATCHED.name, result.message)
        } else {
            dao.updateDraftSyncStatus(savedId, DraftSyncStatus.FAILED.name)
            dao.updateTaskStatus(taskId, TaskDispatchStatus.FAILED.name, result.message)
        }
        return result
    }
}
