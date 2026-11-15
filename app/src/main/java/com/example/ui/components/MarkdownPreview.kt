package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.domain.model.PlatformCatalog
import com.example.domain.model.PlatformFitWarning
import com.example.ui.theme.JetBrainsMonoFontFamily

@Composable
fun MarkdownPreviewView(
    title: String,
    markdown: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (title.isNotBlank()) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
        }

        if (markdown.isBlank()) {
            Text(
                text = "正文预览将在此实时渲染…",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            return@Column
        }

        val lines = markdown.lines()
        var index = 0
        while (index < lines.size) {
            val line = lines[index]
            val trimmed = line.trim()
            when {
                trimmed.startsWith("```") -> {
                    val codeLines = mutableListOf<String>()
                    index++
                    while (index < lines.size && !lines[index].trim().startsWith("```")) {
                        codeLines.add(lines[index])
                        index++
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = codeLines.joinToString("\n"),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = JetBrainsMonoFontFamily
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .horizontalScroll(rememberScrollState())
                                .padding(12.dp)
                        )
                    }
                }
                trimmed.startsWith("### ") -> {
                    Text(
                        text = parseInlineMarkdown(trimmed.removePrefix("### ")),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                trimmed.startsWith("## ") -> {
                    Text(
                        text = parseInlineMarkdown(trimmed.removePrefix("## ")),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                trimmed.startsWith("# ") -> {
                    Text(
                        text = parseInlineMarkdown(trimmed.removePrefix("# ")),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                trimmed.startsWith("> ") -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                            .padding(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(20.dp)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = parseInlineMarkdown(trimmed.removePrefix("> ")),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = parseInlineMarkdown(trimmed.drop(2)),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                trimmed == "---" || trimmed == "***" -> {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                }
                trimmed.isNotEmpty() -> {
                    Text(
                        text = parseInlineMarkdown(line),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            index++
        }
    }
}

@Composable
private fun parseInlineMarkdown(input: String): AnnotatedString {
    val primaryColor = MaterialTheme.colorScheme.primary
    val codeBg = MaterialTheme.colorScheme.surfaceVariant
    return buildAnnotatedString {
        var i = 0
        while (i < input.length) {
            when {
                input.startsWith("**", i) -> {
                    val end = input.indexOf("**", i + 2)
                    if (end != -1) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(input.substring(i + 2, end))
                        }
                        i = end + 2
                    } else {
                        append(input[i])
                        i++
                    }
                }
                input[i] == '`' -> {
                    val end = input.indexOf('`', i + 1)
                    if (end != -1) {
                        withStyle(
                            SpanStyle(
                                fontFamily = JetBrainsMonoFontFamily,
                                background = codeBg,
                                color = primaryColor
                            )
                        ) {
                            append(" ${input.substring(i + 1, end)} ")
                        }
                        i = end + 1
                    } else {
                        append(input[i])
                        i++
                    }
                }
                input[i] == '*' -> {
                    val end = input.indexOf('*', i + 1)
                    if (end != -1) {
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            append(input.substring(i + 1, end))
                        }
                        i = end + 1
                    } else {
                        append(input[i])
                        i++
                    }
                }
                else -> {
                    append(input[i])
                    i++
                }
            }
        }
    }
}

fun analyzePlatformFit(
    title: String,
    body: String,
    selectedPlatformIds: List<String>
): List<PlatformFitWarning> {
    val warnings = mutableListOf<PlatformFitWarning>()
    val titleLen = title.trim().length
    val bodyLen = body.trim().length

    selectedPlatformIds.forEach { id ->
        val spec = PlatformCatalog.findById(id) ?: return@forEach
        if (titleLen > spec.titleMaxChars) {
            warnings.add(
                PlatformFitWarning(
                    platformName = spec.name,
                    message = "标题 ${titleLen} 字超出上限（建议 ≤ ${spec.titleMaxChars} 字）",
                    isBlocking = true
                )
            )
        }
        val bodyLimit = spec.bodyMaxChars
        if (bodyLimit != null && bodyLen > bodyLimit) {
            warnings.add(
                PlatformFitWarning(
                    platformName = spec.name,
                    message = "正文 ${bodyLen} 字超出上限（≤ $bodyLimit 字）",
                    isBlocking = id == "x_twitter"
                )
            )
        }
    }
    return warnings
}
