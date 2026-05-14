package com.example.ecofruit.ui.managers

import android.content.Context
import android.content.res.Configuration
import com.example.ecofruit.ui.data.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.Locale
import androidx.core.content.edit

object LocaleManager {

    fun applyLocale(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = context.resources.configuration.apply {
            setLocale(locale)
        }
        return context.createConfigurationContext(config)
    }
}