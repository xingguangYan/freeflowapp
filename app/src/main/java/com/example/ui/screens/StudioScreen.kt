package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.DraftContentType
import com.example.data.local.DraftEntity
import com.example.data.local.DraftSyncStatus
import com.example.domain.model.ConnectionState
import com.example.domain.model.PlatformCatalog
import com.example.ui.components.MetricBadge
import com.example.ui.components.StatusPill
import com.example.ui.components.formatTimestamp

@Composable
fun StudioScreen(
    drafts: List<DraftEntity>,
    connectionState: ConnectionState,
    onCreateDraft: () -> Unit,
    onLoadTemplate: () -> Unit,
    onOpenDraft: (Long) -> Unit,
    onDeleteDraft: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf<DraftContentType?>(null) }

    val filteredDrafts = remember(drafts, searchQuery, selectedTypeFilter) {
        drafts.filter { draft ->
            val matchesType = selectedTypeFilter == null || draft.resolvedContentType() == selectedTypeFilter
            val q = searchQuery.trim()
            val matchesQuery = q.isEmpty() ||
                draft.title.contains(q, ignoreCase = true) ||
                draft.contentMarkdown.contains(q, ignoreCase = true) ||
                draft.tagsCsv.contains(q, ignoreCase = true)
            matchesType && matchesQuery
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateDraft,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_new_draft")
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.new_draft)
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 760.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    StudioHeaderCard(
                        draftCount = drafts.size,
                        syncedCount = drafts.count { it.resolvedSyncStatus() == DraftSyncStatus.SYNCED },
                        connectionState = connectionState
                    )
                }

                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text(stringResource(R.string.search_drafts_hint)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "搜索草稿"
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("draft_search_input")
                    )
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedTypeFilter == null,
                            onClick = { selectedTypeFilter = null },
                            label = { Text("全部 (${drafts.size})") },
                            modifier = Modifier.testTag("filter_all_drafts")
                        )
                        DraftContentType.entries.forEach { type ->
                            val count = drafts.count { it.resolvedContentType() == type }
                            FilterChip(
                                selected = selectedTypeFilter == type,
                                onClick = {
                                    selectedTypeFilter = if (selectedTypeFilter == type) null else type
                                },
                                label = { Text("${type.label} ($count)") },
                                modifier = Modifier.testTag("filter_${type.name.lowercase()}")
                            )
                        }
                    }
                }

                if (filteredDrafts.isEmpty()) {
                    item {
                        EmptyDraftsCard(
                            hasQuery = searchQuery.isNotBlank() || selectedTypeFilter != null,
                            onCreateDraft = onCreateDraft,
                            onLoadTemplate = onLoadTemplate
                        )
                    }
                } else {
                    items(filteredDrafts, key = { it.id }) { draft ->
                        DraftListCard(
                            draft = draft,
                            onClick = { onOpenDraft(draft.id) },
                            onDelete = { onDeleteDraft(draft.id) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(72.dp)) }
                }
            }
        }
    }
}

@Composable
private fun StudioHeaderCard(
    draftCount: Int,
    syncedCount: Int,
    connectionState: ConnectionState
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.studio_title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.studio_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                val (statusLabel, container, content) = when (connectionState) {
                    is ConnectionState.Connected -> Triple(
                        "已连桌面端",
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    is ConnectionState.Checking -> Triple(
                        "检测中…",
                        MaterialTheme.colorScheme.secondaryContainer,
                        MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    else -> Triple(
                        "离线口袋模式",
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusPill(
                    label = statusLabel,
                    containerColor = container,
                    contentColor = content,
                    dotColor = content
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricBadge(
                    label = "本地草稿",
                    value = "$draftCount 篇",
                    modifier = Modifier.weight(1f)
                )
                MetricBadge(
                    label = "已推送桌面",
                    value = "$syncedCount 篇",
                    modifier = Modifier.weight(1f)
                )
                MetricBadge(
                    label = "支持渠道",
                    value = "${PlatformCatalog.allPlatforms.size} 个",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun EmptyDraftsCard(
    hasQuery: Boolean,
    onCreateDraft: () -> Unit,
    onLoadTemplate: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.EditNote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(44.dp)
            )
            Text(
                text = if (hasQuery) "没有匹配的草稿" else stringResource(R.string.empty_drafts_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (hasQuery) "尝试清空关键词或切换内容类型筛选。" else stringResource(R.string.empty_drafts_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!hasQuery) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
                ) {
                    Button(
                        onClick = onCreateDraft,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("empty_new_draft_button")
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text(" 新建空白稿件")
                    }
                    OutlinedButton(
                        onClick = onLoadTemplate,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("empty_template_button")
                    ) {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text(" 插入示例模板")
                    }
                }
            }
        }
    }
}

@Composable
private fun DraftListCard(
    draft: DraftEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val syncStatus = draft.resolvedSyncStatus()
    val (pillBg, pillFg) = when (syncStatus) {
        DraftSyncStatus.SYNCED -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        DraftSyncStatus.QUEUED -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        DraftSyncStatus.FAILED -> MaterialTheme.colorScheme.error.copy(alpha = 0.14f) to MaterialTheme.colorScheme.error
        DraftSyncStatus.LOCAL_ONLY -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("draft_card_${draft.id}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusPill(
                        label = draft.resolvedContentType().label,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    StatusPill(
                        label = syncStatus.label,
                        containerColor = pillBg,
                        contentColor = pillFg
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("delete_draft_${draft.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.DeleteOutline,
                        contentDescription = "删除草稿",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = draft.title.ifBlank { "无标题草稿" },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (draft.contentMarkdown.isNotBlank()) {
                Text(
                    text = draft.contentMarkdown.replace("\n", " "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            val platformNames = draft.platformIds()
                .mapNotNull { PlatformCatalog.findById(it)?.name }
                .take(3)
                .joinToString(" · ")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (platformNames.isNotBlank()) {
                            "$platformNames (${draft.platformIds().size} 渠道)"
                        } else {
                            "未选择目标平台"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = "${draft.contentMarkdown.length} 字 · ${formatTimestamp(draft.updatedAt)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
