package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.PublishTaskEntity
import com.example.data.local.TaskDispatchStatus
import com.example.domain.model.BridgeConfig
import com.example.domain.model.ConnectionState
import com.example.ui.components.StatusPill
import com.example.ui.components.formatTimestamp

@Composable
fun BridgeScreen(
    bridgeConfig: BridgeConfig,
    connectionState: ConnectionState,
    isSyncingAccounts: Boolean,
    publishTasks: List<PublishTaskEntity>,
    onSaveConfig: (String, String) -> Unit,
    onPingDesktop: (String, String) -> Unit,
    onSyncAccounts: () -> Unit,
    onRetryTask: (PublishTaskEntity) -> Unit,
    onDeleteTask: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var hostInput by remember { mutableStateOf(bridgeConfig.hostAddress) }
    var tokenInput by remember { mutableStateOf(bridgeConfig.pairingToken) }

    LaunchedEffect(bridgeConfig) {
        hostInput = bridgeConfig.hostAddress
        tokenInput = bridgeConfig.pairingToken
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Image(
                            painter = painterResource(id = R.drawable.img_studio_banner),
                            contentDescription = "FreeFlow 手机与桌面端局域网协同示意图",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(146.dp)
                                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        )
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.bridge_title),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(R.string.bridge_subtitle),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "局域网目标主机配置",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        OutlinedTextField(
                            value = hostInput,
                            onValueChange = { hostInput = it },
                            label = { Text("桌面端 IP : 端口") },
                            placeholder = { Text(stringResource(R.string.bridge_host_hint)) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("bridge_host_input")
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AssistChip(
                                onClick = { hostInput = "192.168.1.100:8917" },
                                label = { Text("局域网 :8917") }
                            )
                            AssistChip(
                                onClick = { hostInput = "10.0.2.2:8917" },
                                label = { Text("模拟器直连宿主机 10.0.2.2:8917") }
                            )
                            AssistChip(
                                onClick = { hostInput = "127.0.0.1:8917" },
                                label = { Text("ADB 端口转发 127.0.0.1:8917") }
                            )
                        }

                        OutlinedTextField(
                            value = tokenInput,
                            onValueChange = { tokenInput = it },
                            label = { Text("配对密钥（可选）") },
                            placeholder = { Text(stringResource(R.string.bridge_token_hint)) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("bridge_token_input")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onSaveConfig(hostInput, tokenInput) },
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                                    .testTag("bridge_save_button")
                            ) {
                                Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                                Text(" 保存配置")
                            }
                            Button(
                                onClick = { onPingDesktop(hostInput, tokenInput) },
                                modifier = Modifier
                                    .weight(1.2f)
                                    .minimumInteractiveComponentSize()
                                    .testTag("bridge_ping_button")
                            ) {
                                Icon(Icons.Filled.Lan, contentDescription = null, modifier = Modifier.size(18.dp))
                                Text(" ${stringResource(R.string.bridge_ping)}")
                            }
                        }

                        ConnectionStatusBanner(
                            state = connectionState,
                            isSyncingAccounts = isSyncingAccounts,
                            onSyncAccounts = onSyncAccounts
                        )
                    }
                }
            }

            item {
                Text(
                    text = "分发推送队列 (${publishTasks.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (publishTasks.isEmpty()) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "暂无推送记录。在「创作台」编辑草稿后点击「推送至桌面端发布」，即可在此追踪执行状态。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(publishTasks, key = { it.id }) { task ->
                    PublishTaskCard(
                        task = task,
                        onRetry = { onRetryTask(task) },
                        onDelete = { onDeleteTask(task.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ConnectionStatusBanner(
    state: ConnectionState,
    isSyncingAccounts: Boolean,
    onSyncAccounts: () -> Unit
) {
    when (state) {
        is ConnectionState.Idle -> {
            Text(
                text = "提示：点击「检测连通性」将向 http://<IP>:8917/api/health 发起真实 LAN 探测。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        is ConnectionState.Checking -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                Text(
                    text = "正在探测局域网桌面引擎…",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        is ConnectionState.Connected -> {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "连接成功 · ${state.host} (${state.latencyMs}ms)",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "桌面运行环境：${state.desktopOs} · ${state.engineVersion}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    OutlinedButton(
                        onClick = onSyncAccounts,
                        enabled = !isSyncingAccounts,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("bridge_sync_accounts_button")
                    ) {
                        Icon(Icons.Filled.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text(if (isSyncingAccounts) " 同步中…" else " ${stringResource(R.string.bridge_sync_accounts)}")
                    }
                }
            }
        }
        is ConnectionState.Unreachable -> {
            Surface(
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "桌面端暂不可达",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = state.reason,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun PublishTaskCard(
    task: PublishTaskEntity,
    onRetry: () -> Unit,
    onDelete: () -> Unit
) {
    val status = task.resolvedStatus()
    val (bg, fg) = when (status) {
        TaskDispatchStatus.DISPATCHED -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        TaskDispatchStatus.PENDING -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        TaskDispatchStatus.FAILED -> MaterialTheme.colorScheme.error.copy(alpha = 0.14f) to MaterialTheme.colorScheme.error
    }
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusPill(label = status.label, containerColor = bg, contentColor = fg)
                Row {
                    IconButton(
                        onClick = onRetry,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("retry_task_${task.id}")
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = "重试推送")
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("delete_task_${task.id}")
                    ) {
                        Icon(Icons.Filled.DeleteOutline, contentDescription = "移除任务")
                    }
                }
            }
            Text(
                text = task.draftTitle,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "目标主机：${task.desktopEndpoint} · ${formatTimestamp(task.createdAt)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = task.responseMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
