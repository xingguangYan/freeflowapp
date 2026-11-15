package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.DraftContentType
import com.example.data.local.DraftEntity
import com.example.domain.model.PlatformCatalog
import com.example.ui.components.MarkdownPreviewView
import com.example.ui.components.analyzePlatformFit

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditorScreen(
    existingDraft: DraftEntity?,
    defaultPlatformIds: Set<String>,
    onBack: () -> Unit,
    onSaveDraft: (DraftEntity) -> Unit,
    onDispatchPublish: (DraftEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var title by remember(existingDraft?.id) { mutableStateOf(existingDraft?.title ?: "") }
    var body by remember(existingDraft?.id) { mutableStateOf(existingDraft?.contentMarkdown ?: "") }
    var tagsCsv by remember(existingDraft?.id) { mutableStateOf(existingDraft?.tagsCsv ?: "") }
    var contentType by remember(existingDraft?.id) {
        mutableStateOf(existingDraft?.resolvedContentType() ?: DraftContentType.ARTICLE)
    }
    var coverUri by remember(existingDraft?.id) { mutableStateOf(existingDraft?.coverUri ?: "") }
    var mediaUris by remember(existingDraft?.id) {
        mutableStateOf(existingDraft?.mediaUris() ?: emptyList())
    }
    var selectedPlatforms by remember(existingDraft?.id) {
        mutableStateOf(
            existingDraft?.platformIds()?.toSet()
                ?.takeIf { it.isNotEmpty() }
                ?: defaultPlatformIds
        )
    }
    var isPreviewMode by remember { mutableStateOf(false) }
    var showPlatformSheet by remember { mutableStateOf(false) }

    LaunchedEffect(existingDraft) {
        if (existingDraft != null && title.isEmpty() && body.isEmpty()) {
            title = existingDraft.title
            body = existingDraft.contentMarkdown
            tagsCsv = existingDraft.tagsCsv
            contentType = existingDraft.resolvedContentType()
            coverUri = existingDraft.coverUri
            mediaUris = existingDraft.mediaUris()
            selectedPlatforms = existingDraft.platformIds().toSet()
        }
    }

    // Zero-permission Android Photo Picker
    val coverPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            coverUri = uri.toString()
        }
    }

    val mediaPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 9)
    ) { uris ->
        if (uris.isNotEmpty()) {
            val merged = (mediaUris + uris.map { it.toString() }).distinct()
            mediaUris = merged
        }
    }

    val currentDraft = DraftEntity(
        id = existingDraft?.id ?: 0L,
        title = title,
        contentMarkdown = body,
        contentType = contentType.name,
        tagsCsv = tagsCsv,
        coverUri = coverUri,
        mediaUrisCsv = mediaUris.joinToString(","),
        selectedPlatformIdsCsv = selectedPlatforms.joinToString(","),
        syncStatus = existingDraft?.syncStatus ?: com.example.data.local.DraftSyncStatus.LOCAL_ONLY.name,
        createdAt = existingDraft?.createdAt ?: System.currentTimeMillis()
    )

    val fitWarnings = remember(title, body, selectedPlatforms) {
        analyzePlatformFit(title, body, selectedPlatforms.toList())
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (existingDraft == null) "新建创作稿件" else "编辑草稿",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("editor_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回创作台"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val shareText = buildString {
                                if (title.isNotBlank()) append("# $title\n\n")
                                append(body)
                            }
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, title.ifBlank { "FreeFlow Markdown 稿件" })
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(Intent.createChooser(intent, "导出或分享 Markdown"))
                        },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("editor_share_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = stringResource(R.string.editor_export_share)
                        )
                    }
                    IconButton(
                        onClick = { onSaveDraft(currentDraft) },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("editor_save_icon_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Save,
                            contentDescription = stringResource(R.string.editor_save)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 760.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SingleChoiceSegmentedButtonRow {
                        SegmentedButton(
                            selected = !isPreviewMode,
                            onClick = { isPreviewMode = false },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                            icon = { Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.testTag("editor_tab_edit")
                        ) {
                            Text(stringResource(R.string.editor_mode_edit))
                        }
                        SegmentedButton(
                            selected = isPreviewMode,
                            onClick = { isPreviewMode = true },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                            icon = { Icon(Icons.Filled.Visibility, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.testTag("editor_tab_preview")
                        ) {
                            Text(stringResource(R.string.editor_mode_preview))
                        }
                    }

                    Text(
                        text = "${body.length} 字 · ${((body.length / 400.0).coerceAtLeast(1.0)).toInt()} 分钟阅读",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Content Type selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DraftContentType.entries.forEach { type ->
                        FilterChip(
                            selected = contentType == type,
                            onClick = { contentType = type },
                            label = { Text(type.label) },
                            modifier = Modifier.testTag("type_${type.name.lowercase()}")
                        )
                    }
                    AssistChip(
                        onClick = { showPlatformSheet = true },
                        leadingIcon = { Icon(Icons.Filled.Tune, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        label = { Text("目标渠道 (${selectedPlatforms.size})") },
                        modifier = Modifier.testTag("editor_select_platforms_button")
                    )
                }

                if (fitWarnings.isNotEmpty()) {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.WarningAmber,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "多平台排版规范提醒",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            fitWarnings.forEach { w ->
                                Text(
                                    text = "• ${w.platformName}：${w.message}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                if (!isPreviewMode) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = { Text(stringResource(R.string.editor_title_hint)) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("editor_title_input")
                    )

                    OutlinedTextField(
                        value = tagsCsv,
                        onValueChange = { tagsCsv = it },
                        placeholder = { Text(stringResource(R.string.editor_tags_hint)) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("editor_tags_input")
                    )

                    // Quick Markdown Toolbar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val snippets = listOf(
                            "H1" to "\n# ",
                            "H2" to "\n## ",
                            "加粗" to "**重点内容**",
                            "引用" to "\n> ",
                            "列表" to "\n- ",
                            "代码" to "\n```\n\n```",
                            "分割线" to "\n---\n"
                        )
                        snippets.forEach { (label, snippet) ->
                            AssistChip(
                                onClick = { body += snippet },
                                label = { Text(label) },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("md_tool_$label")
                            )
                        }
                    }

                    OutlinedTextField(
                        value = body,
                        onValueChange = { body = it },
                        placeholder = { Text(stringResource(R.string.editor_body_hint)) },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 240.dp)
                            .testTag("editor_body_input")
                    )
                } else {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 260.dp)
                    ) {
                        MarkdownPreviewView(
                            title = title,
                            markdown = body,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                // Zero-permission Photo Picker media section
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "封面与图集素材（Android 零权限 Photo Picker）",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = {
                                    coverPicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("editor_pick_cover_button")
                            ) {
                                Icon(Icons.Filled.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                                Text(" ${stringResource(R.string.editor_pick_cover)}")
                            }
                            OutlinedButton(
                                onClick = {
                                    mediaPicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                                    )
                                },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("editor_add_media_button")
                            ) {
                                Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                                Text(" ${stringResource(R.string.editor_add_media)}")
                            }
                        }

                        if (coverUri.isNotBlank() || mediaUris.isNotEmpty()) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(top = 4.dp)
                            ) {
                                if (coverUri.isNotBlank()) {
                                    item {
                                        AsyncImage(
                                            model = coverUri,
                                            contentDescription = "文章封面图",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(84.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                        )
                                    }
                                }
                                items(mediaUris) { uri ->
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = "已选多媒体素材",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(84.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                    )
                                }
                            }
                        }
                    }
                }

                // Action Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { onSaveDraft(currentDraft) },
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("editor_save_button")
                    ) {
                        Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text(" ${stringResource(R.string.editor_save)}")
                    }
                    Button(
                        onClick = { onDispatchPublish(currentDraft) },
                        enabled = title.isNotBlank() || body.isNotBlank(),
                        modifier = Modifier
                            .weight(1.3f)
                            .minimumInteractiveComponentSize()
                            .testTag("editor_sync_publish_button")
                    ) {
                        Icon(Icons.Filled.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text(" ${stringResource(R.string.editor_sync_publish)}")
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showPlatformSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPlatformSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "选择本次分发目标渠道",
                    style = MaterialTheme.typography.titleLarge
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PlatformCatalog.presets.forEach { preset ->
                        AssistChip(
                            onClick = { selectedPlatforms = preset.platformIds.toSet() },
                            label = { Text("预设：${preset.name}") }
                        )
                    }
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PlatformCatalog.allPlatforms.forEach { platform ->
                        val isSelected = selectedPlatforms.contains(platform.id)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                val next = selectedPlatforms.toMutableSet()
                                if (isSelected) next.remove(platform.id) else next.add(platform.id)
                                selectedPlatforms = next
                            },
                            label = { Text(platform.name) },
                            modifier = Modifier.testTag("sheet_platform_${platform.id}")
                        )
                    }
                }
                Button(
                    onClick = { showPlatformSheet = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .minimumInteractiveComponentSize()
                ) {
                    Text("确认已选 ${selectedPlatforms.size} 个平台")
                }
            }
        }
    }
}
