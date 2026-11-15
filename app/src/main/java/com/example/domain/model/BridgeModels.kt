package com.example.domain.model

sealed class ConnectionState {
    data object Idle : ConnectionState()
    data object Checking : ConnectionState()
    data class Connected(
        val host: String,
        val latencyMs: Long,
        val engineVersion: String,
        val desktopOs: String
    ) : ConnectionState()

    data class Unreachable(
        val host: String,
        val reason: String
    ) : ConnectionState()
}

data class DesktopAccountStatus(
    val platformId: String,
    val platformName: String,
    val loggedIn: Boolean,
    val accountLabel: String
)

data class BridgeConfig(
    val hostAddress: String = "192.168.1.100:8917",
    val pairingToken: String = ""
)

data class PlatformFitWarning(
    val platformName: String,
    val message: String,
    val isBlocking: Boolean
)
