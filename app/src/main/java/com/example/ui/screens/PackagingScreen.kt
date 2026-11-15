package com.example.ui.screens

import android.os.Build
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.BuildConfig
import com.example.R
import com.example.ui.components.CopyableCodeBlock
import com.example.ui.components.MetricBadge
import com.example.ui.components.StatusPill

@Composable
fun PackagingScreen(
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val supportedAbis = Build.SUPPORTED_ABIS.joinToString(", ")

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
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(R.string.packaging_title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = stringResource(R.string.packaging_subtitle),
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
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "当前 APK 优化状态",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            StatusPill(
                                label = "Native Compose",
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            MetricBadge(
                                label = "应用包名",
                                value = "v${BuildConfig.VERSION_NAME}",
                                modifier = Modifier.weight(0.8f)
                            )
                            MetricBadge(
                                label = "运行架构 (ABI)",
                                value = supportedAbis.ifBlank { "arm64-v8a" },
                                modifier = Modifier.weight(1.4f)
                            )
                        }
                        Text(
                            text = "• 已剔除冗余 WebView / Firebase / Retrofit 依赖，大幅缩减 APK 体积\n" +
                                "• 使用 Android 零权限 Photo Picker 与 Room 本地持久化\n" +
                                "• 已配置 network_security_config 允许局域网 HTTP (:8917) 直连桌面端",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "跨平台架构设计（Android APK + Windows EXE）",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "由于 FreeFlow 依赖 Playwright / Chromium 自动化登录与富文本注入（无法在未 Root 的 Android 沙箱直接运行桌面浏览器），最佳跨平台方案如下：\n\n" +
                                "1. 方案 A（推荐 · 对标原仓库）：Windows 端打包 Electron + Node.js + Playwright 为 .exe 安装包/便携版，并监听 0.0.0.0:8917 局域网 HTTP 桥接；Android 端使用本原生 APK 作为轻量口袋编辑器。\n" +
                                "2. 方案 B（纯 Kotlin 代码复用）：采用 Kotlin Multiplatform + Compose Multiplatform，将创作台 UI、Markdown 解析与网络层放在 commonMain，Android 编译 APK，Windows 通过 jpackage 打包 .exe / .msi。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                CopyableCodeBlock(
                    title = "1. Windows 端防火墙放行 8917 端口（管理员 PowerShell）",
                    code = "New-NetFirewallRule -DisplayName \"FreeFlow LAN Bridge\" -Direction Inbound -Protocol TCP -LocalPort 8917 -Action Allow -Profile Private",
                    tagPrefix = "win_firewall",
                    onCopied = onShowMessage
                )
            }

            item {
                CopyableCodeBlock(
                    title = "2. Windows .exe / 便携版打包脚本 (electron-builder)",
                    code = """
                        npm ci
                        npx playwright install chromium
                        npm run build
                        npx electron-builder --win nsis zip --x64
                    """.trimIndent(),
                    tagPrefix = "win_electron_build",
                    onCopied = onShowMessage
                )
            }

            item {
                CopyableCodeBlock(
                    title = "3. Compose Desktop 打包 Windows .exe (KMP 方案)",
                    code = """
                        # 生成自带精简 JRE 的 Windows 安装包 (.exe / .msi)
                        gradle :desktopApp:packageExe
                        gradle :desktopApp:createDistributable
                    """.trimIndent(),
                    tagPrefix = "win_kmp_build",
                    onCopied = onShowMessage
                )
            }

            item {
                CopyableCodeBlock(
                    title = "4. Android 优化发行包构建命令 (.aab / .apk)",
                    code = """
                        # 生成分架构签名的 Release APK 与 Google Play App Bundle
                        gradle :app:assembleRelease
                        gradle :app:bundleRelease
                    """.trimIndent(),
                    tagPrefix = "android_release_build",
                    onCopied = onShowMessage
                )
            }
        }
    }
}
