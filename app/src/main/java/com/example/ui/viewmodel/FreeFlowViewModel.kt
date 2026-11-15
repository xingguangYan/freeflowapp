package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.BridgePreferences
import com.example.data.local.DraftContentType
import com.example.data.local.DraftEntity
import com.example.data.local.FreeFlowDatabase
import com.example.data.local.PublishTaskEntity
import com.example.data.remote.FreeFlowBridgeClient
import com.example.data.repository.FreeFlowRepository
import com.example.domain.model.BridgeConfig
import com.example.domain.model.ConnectionState
import com.example.domain.model.DesktopAccountStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FreeFlowViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: FreeFlowRepository

    init {
        val db = FreeFlowDatabase.getInstance(application)
        val prefs = BridgePreferences(application)
        val client = FreeFlowBridgeClient()
        repository = FreeFlowRepository(db.freeFlowDao(), prefs, client)
    }

    val drafts: StateFlow<List<DraftEntity>> = repository.drafts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val publishTasks: StateFlow<List<PublishTaskEntity>> = repository.publishTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bridgeConfig: StateFlow<BridgeConfig> = repository.bridgeConfig
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BridgeConfig())

    val defaultPlatformIds: StateFlow<Set<String>> = repository.defaultPlatformIds
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            setOf("wechat_mp", "zhihu", "juejin")
        )

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Idle)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _desktopAccounts = MutableStateFlow<List<DesktopAccountStatus>>(emptyList())
    val desktopAccounts: StateFlow<List<DesktopAccountStatus>> = _desktopAccounts.asStateFlow()

    private val _isSyncingAccounts = MutableStateFlow(false)
    val isSyncingAccounts: StateFlow<Boolean> = _isSyncingAccounts.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun showMessage(message: String) {
        _snackbarMessage.value = message
    }

    fun observeDraft(id: Long) = repository.observeDraft(id)

    fun saveDraft(
        draft: DraftEntity,
        onSaved: (Long) -> Unit = {}
    ) {
        viewModelScope.launch {
            val savedId = repository.saveDraft(draft)
            _snackbarMessage.value = "草稿已安全保存至本地 SQLite"
            onSaved(savedId)
        }
    }

    fun insertStarterMarkdownTemplate(onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val defaultPlatforms = defaultPlatformIds.value.joinToString(",")
            val templateDraft = DraftEntity(
                title = "本地优先创作者工作流：一次排版多端同步",
                contentType = DraftContentType.ARTICLE.name,
                tagsCsv = "FreeFlow,跨平台效率,Markdown",
                selectedPlatformIdsCsv = defaultPlatforms.ifBlank { "wechat_mp,zhihu,juejin" },
                contentMarkdown = """
                    # 为什么选择本地优先的多平台分发

                    在自媒体与技术写作中，最耗时的是在十几个后台之间反复复制粘贴与重新排版。

                    ## 核心工作流
                    - **手机端随手写**：利用碎片时间在 Android 创作台完成 Markdown 草稿与选图
                    - **规范自动校验**：实时检测各平台标题长度与正文字符限制
                    - **桌面端真执行**：通过本地 Wi-Fi 推送至 Windows / macOS 桌面引擎完成分发

                    > 数据与账号会话始终留在你的设备中，不经过任何第三方云端中转。

                    ```bash
                    # Windows / macOS 桌面端默认桥接端口
                    http://192.168.1.100:8917/api/health
                    ```
                """.trimIndent()
            )
            val id = repository.saveDraft(templateDraft)
            _snackbarMessage.value = "已创建多平台排版模板草稿"
            onCreated(id)
        }
    }

    fun deleteDraft(id: Long) {
        viewModelScope.launch {
            repository.deleteDraft(id)
            _snackbarMessage.value = "已删除草稿"
        }
    }

    fun deleteTask(id: Long) {
        viewModelScope.launch {
            repository.deleteTask(id)
        }
    }

    fun saveBridgeConfig(hostAddress: String, pairingToken: String) {
        viewModelScope.launch {
            repository.saveBridgeConfig(hostAddress, pairingToken)
            _snackbarMessage.value = "已更新桌面端桥接地址"
        }
    }

    fun toggleDefaultPlatform(platformId: String) {
        viewModelScope.launch {
            val current = defaultPlatformIds.value.toMutableSet()
            if (current.contains(platformId)) {
                current.remove(platformId)
            } else {
                current.add(platformId)
            }
            repository.saveDefaultPlatforms(current)
        }
    }

    fun applyDefaultPreset(platformIds: List<String>) {
        viewModelScope.launch {
            repository.saveDefaultPlatforms(platformIds.toSet())
            _snackbarMessage.value = "已切换默认分发预设（共 ${platformIds.size} 个平台）"
        }
    }

    fun pingDesktop(hostOverride: String? = null, tokenOverride: String? = null) {
        viewModelScope.launch {
            val config = BridgeConfig(
                hostAddress = hostOverride ?: bridgeConfig.value.hostAddress,
                pairingToken = tokenOverride ?: bridgeConfig.value.pairingToken
            )
            if (hostOverride != null) {
                repository.saveBridgeConfig(config.hostAddress, config.pairingToken)
            }
            _connectionState.value = ConnectionState.Checking
            val result = repository.pingDesktop(config)
            _connectionState.value = result
        }
    }

    fun syncDesktopAccounts() {
        viewModelScope.launch {
            _isSyncingAccounts.value = true
            val result = repository.fetchDesktopAccounts(bridgeConfig.value)
            _isSyncingAccounts.value = false
            result.onSuccess { accounts ->
                _desktopAccounts.value = accounts
                _snackbarMessage.value = if (accounts.isEmpty()) {
                    "桌面端已连接，但尚未检测到已登录渠道"
                } else {
                    "已同步 ${accounts.size} 个桌面端渠道状态"
                }
            }.onFailure { err ->
                _snackbarMessage.value = err.message ?: "同步账号状态失败"
            }
        }
    }

    fun publishDraftToDesktop(draft: DraftEntity) {
        viewModelScope.launch {
            val res = repository.dispatchPublish(draft, bridgeConfig.value)
            _snackbarMessage.value = res.message
        }
    }

    fun retryTask(task: PublishTaskEntity) {
        viewModelScope.launch {
            val draft = repository.getDraft(task.draftId)
            if (draft == null) {
                _snackbarMessage.value = "原草稿已被删除，无法重试"
                return@launch
            }
            val res = repository.dispatchPublish(draft, bridgeConfig.value)
            _snackbarMessage.value = res.message
        }
    }
}
