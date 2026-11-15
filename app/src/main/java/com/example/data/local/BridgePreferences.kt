package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.model.BridgeConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.bridgeDataStore: DataStore<Preferences> by preferencesDataStore(name = "freeflow_bridge_prefs")

class BridgePreferences(private val context: Context) {
    private object Keys {
        val HOST_ADDRESS = stringPreferencesKey("host_address")
        val PAIRING_TOKEN = stringPreferencesKey("pairing_token")
        val DEFAULT_PLATFORMS = stringPreferencesKey("default_platforms")
    }

    val bridgeConfig: Flow<BridgeConfig> = context.bridgeDataStore.data.map { prefs ->
        BridgeConfig(
            hostAddress = prefs[Keys.HOST_ADDRESS] ?: "192.168.1.100:8917",
            pairingToken = prefs[Keys.PAIRING_TOKEN] ?: ""
        )
    }

    val defaultPlatformIds: Flow<Set<String>> = context.bridgeDataStore.data.map { prefs ->
        val raw = prefs[Keys.DEFAULT_PLATFORMS] ?: "wechat_mp,zhihu,juejin"
        raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
    }

    suspend fun saveBridgeConfig(hostAddress: String, pairingToken: String) {
        context.bridgeDataStore.edit { prefs ->
            prefs[Keys.HOST_ADDRESS] = hostAddress.trim()
                .removePrefix("http://")
                .removePrefix("https://")
                .trimEnd('/')
            prefs[Keys.PAIRING_TOKEN] = pairingToken.trim()
        }
    }

    suspend fun saveDefaultPlatforms(platformIds: Set<String>) {
        context.bridgeDataStore.edit { prefs ->
            prefs[Keys.DEFAULT_PLATFORMS] = platformIds.joinToString(",")
        }
    }
}
