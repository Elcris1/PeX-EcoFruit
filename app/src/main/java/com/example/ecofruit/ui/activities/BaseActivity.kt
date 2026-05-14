package com.example.ecofruit.ui.activities

import android.content.Context
import androidx.activity.ComponentActivity
import com.example.ecofruit.ui.data.repository.SettingsKeys
import com.example.ecofruit.ui.data.repository.settingsDataStore
import com.example.ecofruit.ui.managers.LocaleManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

abstract class BaseActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        val language = runBlocking {
            newBase.settingsDataStore.data
                .map { it[SettingsKeys.LANGUAGE] ?: "es" }
                .first()
        }
        super.attachBaseContext(LocaleManager.applyLocale(newBase, language))
    }
}