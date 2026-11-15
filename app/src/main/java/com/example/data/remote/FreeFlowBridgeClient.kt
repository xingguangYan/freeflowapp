package com.example.data.remote

import com.example.data.local.DraftEntity
import com.example.domain.model.ConnectionState
import com.example.domain.model.DesktopAccountStatus
import com.example.domain.model.PlatformCatalog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

data class DispatchResult(
    val success: Boolean,
    val message: String
)

class FreeFlowBridgeClient {
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(4, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    private fun buildBaseUrl(hostAddress: String): String {
        val cleaned = hostAddress.trim()
            .removePrefix("http://")
            .removePrefix("https://")
            .trimEnd('/')
        return "http://$cleaned"
    }

    private fun Request.Builder.withToken(token: String): Request.Builder {
        if (token.isNotBlank()) {
            addHeader("Authorization", "Bearer $token")
            addHeader("X-FreeFlow-Token", token)
        }
        return this
    }

    suspend fun pingHealth(hostAddress: String, token: String): ConnectionState = withContext(Dispatchers.IO) {
        val cleanHost = hostAddress.trim()
        if (cleanHost.isEmpty()) {
            return@withContext ConnectionState.Unreachable(cleanHost, "请先输入桌面端局域网 IP 与端口（如 192.168.1.100:8917）")
        }
        val url = "${buildBaseUrl(cleanHost)}/api/health"
        val start = System.currentTimeMillis()
        val request = Request.Builder()
            .url(url)
            .get()
            .withToken(token)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val latency = (System.currentTimeMillis() - start).coerceAtLeast(1L)
                val bodyStr = response.body?.string().orEmpty()
                if (response.isSuccessful) {
                    val json = runCatching { JSONObject(bodyStr) }.getOrNull()
                    val version = json?.optString("version").takeUnless { it.isNullOrBlank() } ?: "Desktop Bridge"
                    val os = json?.optString("platform").takeUnless { it.isNullOrBlank() }
                        ?: json?.optString("os").takeUnless { it.isNullOrBlank() }
                        ?: "Windows / macOS"
                    ConnectionState.Connected(
                        host = cleanHost,
                        latencyMs = latency,
                        engineVersion = version,
                        desktopOs = os
                    )
                } else {
                    ConnectionState.Unreachable(
                        host = cleanHost,
                        reason = "HTTP ${response.code}: 桌面端拒绝或接口未就绪"
                    )
                }
            }
        } catch (e: IOException) {
            ConnectionState.Unreachable(
                host = cleanHost,
                reason = diagnoseNetworkError(e, cleanHost)
            )
        } catch (e: IllegalArgumentException) {
            ConnectionState.Unreachable(
                host = cleanHost,
                reason = "地址格式无效：请检查 IP 与端口号（例如 192.168.1.100:8917）"
            )
        }
    }

    suspend fun fetchAccounts(hostAddress: String, token: String): Result<List<DesktopAccountStatus>> =
        withContext(Dispatchers.IO) {
            val url = "${buildBaseUrl(hostAddress)}/api/accounts"
            val request = Request.Builder()
                .url(url)
                .get()
                .withToken(token)
                .build()
            try {
                client.newCall(request).execute().use { response ->
                    val raw = response.body?.string().orEmpty()
                    if (!response.isSuccessful) {
                        return@withContext Result.failure(IOException("HTTP ${response.code}: 无法获取桌面端账号列表"))
                    }
                    val parsed = parseAccountsPayload(raw)
                    Result.success(parsed)
                }
            } catch (e: Exception) {
                Result.failure(IOException(diagnoseNetworkError(e, hostAddress)))
            }
        }

    suspend fun dispatchDraftPublish(
        hostAddress: String,
        token: String,
        draft: DraftEntity
    ): DispatchResult = withContext(Dispatchers.IO) {
        val url = "${buildBaseUrl(hostAddress)}/api/publish"
        val payload = JSONObject().apply {
            put("title", draft.title)
            put("content", draft.contentMarkdown)
            put("format", "markdown")
            put("contentType", draft.contentType)
            put("tags", JSONArray(draft.tags()))
            put("platforms", JSONArray(draft.platformIds()))
            put("mediaCount", draft.mediaUris().size + if (draft.coverUri.isNotBlank()) 1 else 0)
            put("source", "android-companion")
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val request = Request.Builder()
            .url(url)
            .post(payload.toString().toRequestBody(mediaType))
            .withToken(token)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (response.isSuccessful) {
                    val json = runCatching { JSONObject(body) }.getOrNull()
                    val msg = json?.optString("message").takeUnless { it.isNullOrBlank() }
                        ?: "已送达桌面端，共 ${draft.platformIds().size} 个目标渠道"
                    DispatchResult(success = true, message = msg)
                } else {
                    DispatchResult(
                        success = false,
                        message = "HTTP ${response.code}: ${body.take(120).ifBlank { "桌面端返回错误" }}"
                    )
                }
            }
        } catch (e: Exception) {
            DispatchResult(
                success = false,
                message = diagnoseNetworkError(e, hostAddress)
            )
        }
    }

    private fun parseAccountsPayload(raw: String): List<DesktopAccountStatus> {
        val result = mutableListOf<DesktopAccountStatus>()
        val trimmed = raw.trim()
        val array = when {
            trimmed.startsWith("[") -> runCatching { JSONArray(trimmed) }.getOrNull()
            trimmed.startsWith("{") -> {
                val obj = runCatching { JSONObject(trimmed) }.getOrNull()
                obj?.optJSONArray("accounts") ?: obj?.optJSONArray("data")
            }
            else -> null
        } ?: return emptyList()

        for (i in 0 until array.length()) {
            val item = array.optJSONObject(i) ?: continue
            val id = item.optString("id").ifBlank { item.optString("platform") }
            val name = item.optString("name").ifBlank {
                PlatformCatalog.findById(id)?.name ?: id
            }
            val loggedIn = item.optBoolean("loggedIn", item.optBoolean("isLogin", false))
            val label = item.optString("nickname").ifBlank {
                item.optString("account").ifBlank { if (loggedIn) "已登录" else "未登录" }
            }
            if (id.isNotBlank()) {
                result.add(
                    DesktopAccountStatus(
                        platformId = id,
                        platformName = name,
                        loggedIn = loggedIn,
                        accountLabel = label
                    )
                )
            }
        }
        return result
    }

    private fun diagnoseNetworkError(error: Throwable, host: String): String {
        val msg = error.message.orEmpty()
        return when {
            msg.contains("timeout", ignoreCase = true) ->
                "连接 $host 超时：请确认 Windows 防火墙已放行 8917 端口且手机与电脑在同一 Wi-Fi"
            msg.contains("ECONNREFUSED", ignoreCase = true) || msg.contains("Failed to connect", ignoreCase = true) ->
                "拒绝连接 $host：请检查 FreeFlow 桌面端是否已启动并开启局域网远程服务"
            else -> "无法连接桌面端 ($host)：${msg.ifBlank { error.javaClass.simpleName }}"
        }
    }
}
