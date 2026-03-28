package com.example.ecofruit.ui.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.ecofruit.ui.data.model.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException


// ── DataStore singleton extension ──────────────────────────────────────────────
val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "ecofruit_settings"
)

// ── Preference keys ────────────────────────────────────────────────────────────
object SettingsKeys {
    // Network
    val WIFI_ONLY_MODE      = booleanPreferencesKey("wifi_only_mode")
    val DATA_SAVER          = booleanPreferencesKey("data_saver")
    val AUTO_SYNC           = booleanPreferencesKey("auto_sync")
    val SYNC_FREQUENCY      = stringPreferencesKey("sync_frequency")   // "15min" | "30min" | "1h" | "manual"
    val CONNECTION_TIMEOUT  = intPreferencesKey("connection_timeout")  // seconds: 10 | 20 | 30 | 60
    val MAX_CACHE_SIZE_MB   = intPreferencesKey("max_cache_size_mb")   // 50 | 100 | 250 | 500
    val OFFLINE_MODE        = booleanPreferencesKey("offline_mode")
    val PRELOAD_IMAGES      = booleanPreferencesKey("preload_images")

    // General
    val DARK_THEME          = booleanPreferencesKey("dark_theme")
    val MODIFIED_BY_USER    = booleanPreferencesKey("modified_by_user")
    val NOTIFICATIONS       = booleanPreferencesKey("notifications")
    val LANGUAGE            = stringPreferencesKey("language")         // "es" | "en" | "ca"
}



class SettingsRepository(private val context: Context) {

    val settingsFlow: Flow<Settings> = context.settingsDataStore.data
        .catch { e ->
            if (e is IOException) emit(emptyPreferences())
            else throw e
        }
        .map { prefs ->
            Settings(
                wifiOnlyMode = prefs[SettingsKeys.WIFI_ONLY_MODE] ?: false,
                dataSaver = prefs[SettingsKeys.DATA_SAVER] ?: false,
                autoSync = prefs[SettingsKeys.AUTO_SYNC] ?: true,
                syncFrequency = prefs[SettingsKeys.SYNC_FREQUENCY] ?: "30min",
                connectionTimeout = prefs[SettingsKeys.CONNECTION_TIMEOUT] ?: 20,
                maxCacheSizeMb = prefs[SettingsKeys.MAX_CACHE_SIZE_MB] ?: 100,
                offlineMode = prefs[SettingsKeys.OFFLINE_MODE] ?: false,
                preloadImages = prefs[SettingsKeys.PRELOAD_IMAGES] ?: true,
                darkTheme = prefs[SettingsKeys.DARK_THEME]?: false,
                modifiedByUser = prefs[SettingsKeys.MODIFIED_BY_USER]?: false,
                notifications = prefs[SettingsKeys.NOTIFICATIONS] ?: true,
                language = prefs[SettingsKeys.LANGUAGE] ?: "es",
            )
        }

    // ── Individual update helpers ──────────────────────────────────────────────

    suspend fun setWifiOnlyMode(value: Boolean) = context.settingsDataStore.edit {
        it[SettingsKeys.WIFI_ONLY_MODE] = value
    }

    suspend fun setDataSaver(value: Boolean) = context.settingsDataStore.edit {
        it[SettingsKeys.DATA_SAVER] = value
    }

    suspend fun setAutoSync(value: Boolean) = context.settingsDataStore.edit {
        it[SettingsKeys.AUTO_SYNC] = value
    }

    suspend fun setSyncFrequency(value: String) = context.settingsDataStore.edit {
        it[SettingsKeys.SYNC_FREQUENCY] = value
    }

    suspend fun setConnectionTimeout(value: Int) = context.settingsDataStore.edit {
        it[SettingsKeys.CONNECTION_TIMEOUT] = value
    }

    suspend fun setMaxCacheSizeMb(value: Int) = context.settingsDataStore.edit {
        it[SettingsKeys.MAX_CACHE_SIZE_MB] = value
    }

    suspend fun setOfflineMode(value: Boolean) = context.settingsDataStore.edit {
        it[SettingsKeys.OFFLINE_MODE] = value
    }

    suspend fun setPreloadImages(value: Boolean) = context.settingsDataStore.edit {
        it[SettingsKeys.PRELOAD_IMAGES] = value
    }

    suspend fun setDarkTheme(value: Boolean, modified_by_user: Boolean = true) = context.settingsDataStore.edit {
        it[SettingsKeys.DARK_THEME] = value
        it[SettingsKeys.MODIFIED_BY_USER] = modified_by_user
    }

    suspend fun setNotifications(value: Boolean) = context.settingsDataStore.edit {
        it[SettingsKeys.NOTIFICATIONS] = value
    }

    suspend fun setLanguage(value: String) = context.settingsDataStore.edit {
        it[SettingsKeys.LANGUAGE] = value
    }

    /** Wipe all preferences back to defaults */
    suspend fun resetAll() = context.settingsDataStore.edit { it.clear() }
}