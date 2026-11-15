package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.model.DesktopAccountStatus
import com.example.domain.model.PlatformCatalog
import com.example.domain.model.PlatformCategory
import com.example.domain.model.PlatformSpec
import com.example.ui.components.StatusPill

@Composable
fun PlatformsScreen(
    defaultPlatformIds: Set<String>,
    desktopAccounts: List<DesktopAccountStatus>,
    onToggleDefaultPlatform: (String) -> Unit,
    onApplyPreset: (List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf<PlatformCategory?>(null) }

    val visiblePlatforms = remember(selectedCategory) {
        if (selectedCategory == null) {
            PlatformCatalog.allPlatforms
        } else {
            PlatformCatalog.allPlatforms.filter { it.category == selectedCategory }
        }
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(R.string.platforms_title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = stringResource(R.string.platforms_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "一键切换默认分发预设（新建草稿自动应用）",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PlatformCatalog.presets.forEach { preset ->
                                AssistChip(
                                    onClick = { onApplyPreset(preset.platformIds) },
                                    label = { Text("${preset.name} (${preset.platformIds.size})") },
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("preset_${preset.id}")
                                )
                            }
                        }
                        Text(
                            text = "当前默认已勾选 ${defaultPlatformIds.size} 个目标渠道",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                        label = { Text("全部 (${PlatformCatalog.allPlatforms.size})") },
                        modifier = Modifier.testTag("category_all")
                    )
                    PlatformCategory.entries.forEach { cat ->
                        val count = PlatformCatalog.allPlatforms.count { it.category == cat }
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = if (selectedCategory == cat) null else cat },
                            label = { Text("${cat.label} ($count)") },
                            modifier = Modifier.testTag("category_${cat.name.lowercase()}")
                        )
                    }
                }
            }

            items(visiblePlatforms, key = { it.id }) { spec ->
                val account = desktopAccounts.find { it.platformId == spec.id }
                PlatformSpecCard(
                    spec = spec,
                    isDefaultSelected = defaultPlatformIds.contains(spec.id),
                    accountStatus = account,
                    onToggle = { onToggleDefaultPlatform(spec.id) }
                )
            }
        }
    }
}

@Composable
private fun PlatformSpecCard(
    spec: PlatformSpec,
    isDefaultSelected: Boolean,
    accountStatus: DesktopAccountStatus?,
    onToggle: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (isDefaultSelected) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.65f)
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            }
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = spec.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    StatusPill(
                        label = if (spec.supportsMarkdown) "Markdown 原生" else "富文本/短文案",
                        containerColor = if (spec.supportsMarkdown) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        contentColor = if (spec.supportsMarkdown) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }

                Text(
                    text = spec.highlightNote,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "标题 ≤${spec.titleMaxChars} 字" +
                            (spec.bodyMaxChars?.let { " · 正文 ≤$it 字" } ?: " · 长文无上限"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                val sessionText = when {
                    accountStatus == null -> "桌面端会话：未同步（连接后校验登录态）"
                    accountStatus.loggedIn -> "桌面端已登录：${accountStatus.accountLabel}"
                    else -> "桌面端未登录（需在 Windows/macOS 扫码）"
                }
                Text(
                    text = sessionText,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (accountStatus?.loggedIn == true) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            Checkbox(
                checked = isDefaultSelected,
                onCheckedChange = { onToggle() },
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("platform_checkbox_${spec.id}")
            )
        }
    }
}
