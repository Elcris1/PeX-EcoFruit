package com.example.ecofruit.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecofruit.ui.data.model.Settings
import com.example.ecofruit.ui.data.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    app: Application
): AndroidViewModel(app) {
    private val repo = SettingsRepository(app)

    val settings: StateFlow<Settings> = repo.settingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = Settings()
        )

    // ── Network ───────────────────────────────────────────────────────────────
    fun setWifiOnlyMode(v: Boolean)      = viewModelScope.launch { repo.setWifiOnlyMode(v) }
    fun setDataSaver(v: Boolean)         = viewModelScope.launch { repo.setDataSaver(v) }
    fun setAutoSync(v: Boolean)          = viewModelScope.launch { repo.setAutoSync(v) }
    fun setSyncFrequency(v: String)      = viewModelScope.launch { repo.setSyncFrequency(v) }
    fun setConnectionTimeout(v: Int)     = viewModelScope.launch { repo.setConnectionTimeout(v) }
    fun setMaxCacheSizeMb(v: Int)        = viewModelScope.launch { repo.setMaxCacheSizeMb(v) }
    fun setOfflineMode(v: Boolean)       = viewModelScope.launch { repo.setOfflineMode(v) }
    fun setPreloadImages(v: Boolean)     = viewModelScope.launch { repo.setPreloadImages(v) }

    // ── General ───────────────────────────────────────────────────────────────
    fun setDarkTheme(v: Boolean)         = viewModelScope.launch { repo.setDarkTheme(v) }
    fun setNotifications(v: Boolean)     = viewModelScope.launch { repo.setNotifications(v) }
    fun setLanguage(v: String)           = viewModelScope.launch { repo.setLanguage(v) }

    fun resetAll()                       = viewModelScope.launch { repo.resetAll() }

}