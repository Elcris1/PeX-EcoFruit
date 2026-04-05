package com.example.ecofruit.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ecofruit.ui.theme.EcoFruitTheme
import com.example.ecofruit.ui.viewmodels.SettingsViewModel
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ecofruit.ui.data.model.Settings
import com.example.ecofruit.R

class SettingsActivity : ComponentActivity() {
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by settingsViewModel.settings.collectAsStateWithLifecycle()

            EcoFruitTheme (darkTheme = settings.darkTheme) {
                SettingsScreen(
                    settings  = settings,
                    viewModel = settingsViewModel,
                    onBack    = { finish() }
                )

            }
        }
    }
}


// ── Root screen ────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: Settings,
    viewModel: SettingsViewModel = viewModel() ,
    onBack: () -> Unit,
) {
    var showResetDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text       = stringResource(R.string.settings),
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.onPrimary,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint               = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->

        LazyColumn(
            modifier            = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {

            // ── Section: General ──────────────────────────────────────────────
            item { SettingsSectionHeader(title = stringResource(R.string.general), icon = Icons.Outlined.Settings) }

            item {
                SettingsCard {
                    SwitchPreferenceRow(
                        icon        = Icons.Outlined.DarkMode,
                        title       = stringResource(R.string.dark_theme_title),
                        subtitle    = stringResource(R.string.dark_theme_subtitle),
                        checked     = settings.darkTheme,
                        onCheckedChange = viewModel::setDarkTheme,
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    SwitchPreferenceRow(
                        icon        = Icons.Outlined.Notifications,
                        title       =  stringResource(R.string.notifications),
                        subtitle    = stringResource(R.string.notifications_subtitle),
                        checked     = settings.notifications,
                        onCheckedChange = viewModel::setNotifications,
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    DropdownPreferenceRow(
                        icon     = Icons.Outlined.Language,
                        title    = stringResource(R.string.language),
                        current  = when (settings.language) {
                            "es" -> stringResource(R.string.language_spanish)
                            "en" -> stringResource(R.string.language_english)
                            "ca" -> stringResource(R.string.language_catalan)
                            else -> settings.language
                        },
                        options  = listOf("es" to stringResource(R.string.language_spanish), "en" to stringResource(R.string.language_english), "ca" to stringResource(R.string.language_catalan)),
                        onSelect = viewModel::setLanguage,
                    )
                }
            }

            // ── Section: Red ──────────────────────────────────────────────────
            item { SettingsSectionHeader(title = stringResource(R.string.network), icon = Icons.Outlined.Wifi) }

            item {
                SettingsCard {
                    SwitchPreferenceRow(
                        icon        = Icons.Outlined.WifiOff,
                        title       =  stringResource(R.string.only_wifi),
                        subtitle    = stringResource(R.string.only_wifi_description),
                        checked     = settings.wifiOnlyMode,
                        onCheckedChange = viewModel::setWifiOnlyMode,
                    )
                    /*
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    SwitchPreferenceRow(
                        icon        = Icons.Outlined.DataSaverOn,
                        title       = "Ahorro de datos",
                        subtitle    = "Reducir calidad de imágenes y sincronización",
                        checked     = settings.dataSaver,
                        onCheckedChange = viewModel::setDataSaver,
                        enabled     = !settings.offlineMode,
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    SwitchPreferenceRow(
                        icon        = Icons.Outlined.CloudOff,
                        title       = "Modo sin conexión",
                        subtitle    = "Usar solo datos almacenados en caché",
                        checked     = settings.offlineMode,
                        onCheckedChange = viewModel::setOfflineMode,
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    SwitchPreferenceRow(
                        icon        = Icons.Outlined.Image,
                        title       = "Precargar imágenes",
                        subtitle    = "Cargar fotos de productos anticipadamente",
                        checked     = settings.preloadImages,
                        onCheckedChange = viewModel::setPreloadImages,
                        enabled     = !settings.offlineMode && !settings.dataSaver,
                    )

                     */
                }
            }

            /*
            // ── Section: Sincronización ───────────────────────────────────────
            item { SettingsSectionHeader(title = "Sincronización", icon = Icons.Outlined.Sync) }

            item {
                SettingsCard {
                    SwitchPreferenceRow(
                        icon        = Icons.Outlined.Autorenew,
                        title       = "Sincronización automática",
                        subtitle    = "Mantener datos actualizados en segundo plano",
                        checked     = settings.autoSync,
                        onCheckedChange = viewModel::setAutoSync,
                        enabled     = !settings.offlineMode,
                    )
                    AnimatedVisibility(
                        visible = settings.autoSync && !settings.offlineMode,
                        enter   = expandVertically(),
                        exit    = shrinkVertically(),
                    ) {
                        Column {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            DropdownPreferenceRow(
                                icon     = Icons.Outlined.Schedule,
                                title    = "Frecuencia de sincronización",
                                current  = when (settings.syncFrequency) {
                                    "15min"  -> "Cada 15 minutos"
                                    "30min"  -> "Cada 30 minutos"
                                    "1h"     -> "Cada hora"
                                    "manual" -> "Manual"
                                    else     -> settings.syncFrequency
                                },
                                options  = listOf(
                                    "15min"  to "Cada 15 minutos",
                                    "30min"  to "Cada 30 minutos",
                                    "1h"     to "Cada hora",
                                    "manual" to "Manual",
                                ),
                                onSelect = viewModel::setSyncFrequency,
                            )
                        }
                    }
                }
            }

            // ── Section: Avanzado ─────────────────────────────────────────────
            item { SettingsSectionHeader(title = "Avanzado", icon = Icons.Outlined.Tune) }

            item {
                SettingsCard {
                    DropdownPreferenceRow(
                        icon     = Icons.Outlined.Timer,
                        title    = "Tiempo de espera de conexión",
                        current  = "${settings.connectionTimeout} segundos",
                        options  = listOf(10 to "10 segundos", 20 to "20 segundos", 30 to "30 segundos", 60 to "60 segundos")
                            .map { (k, v) -> k.toString() to v },
                        onSelect = { viewModel.setConnectionTimeout(it.toInt()) },
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    DropdownPreferenceRow(
                        icon     = Icons.Outlined.Storage,
                        title    = "Tamaño máximo de caché",
                        current  = "${settings.maxCacheSizeMb} MB",
                        options  = listOf(50 to "50 MB", 100 to "100 MB", 250 to "250 MB", 500 to "500 MB")
                            .map { (k, v) -> k.toString() to v },
                        onSelect = { viewModel.setMaxCacheSizeMb(it.toInt()) },
                    )
                }
            }

             */

            // ── Danger zone ───────────────────────────────────────────────────
            item { Spacer(Modifier.height(8.dp)) }
            item {
                OutlinedButton(
                    onClick  = { showResetDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border   = ButtonDefaults.outlinedButtonBorder.copy(
                        /* tint border with error color */
                    ),
                ) {
                    Icon(
                        imageVector        = Icons.Outlined.RestartAlt,
                        contentDescription = null,
                        modifier           = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.reset_settings))
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }

    // ── Reset confirmation dialog ──────────────────────────────────────────────
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            icon  = {
                Icon(
                    imageVector = Icons.Outlined.RestartAlt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                )
            },
            title = { Text(stringResource(R.string.reset_settings_ttile)) },
            text  = { Text(stringResource(R.string.reset_settings_description)) },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.resetAll(); showResetDialog = false },
                    colors  = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) { Text(stringResource(R.string.reset)) }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text(stringResource(R.string.cancel)) }
            },
        )
    }
}

// ── Reusable components ────────────────────────────────────────────────────────

@Composable
private fun SettingsSectionHeader(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier          = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp),
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = MaterialTheme.colorScheme.primary,
            modifier           = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text       = title.uppercase(),
            style      = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.primary,
            letterSpacing = androidx.compose.ui.unit.TextUnit(1.5f, androidx.compose.ui.unit.TextUnitType.Sp),
        )
    }
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape  = RoundedCornerShape(16.dp),
        color  = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 0.dp,
    ) {
        Column(modifier = Modifier.fillMaxWidth(), content = content)
    }
}

@Composable
private fun SwitchPreferenceRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
) {
    val alpha = if (enabled) 1f else 0.4f

    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier          = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = alpha)),
            contentAlignment  = Alignment.Center,
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = alpha),
                modifier           = Modifier.size(20.dp),
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text  = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
                fontWeight = FontWeight.Medium,
            )
            Text(
                text  = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha),
            )
        }
        Spacer(Modifier.width(8.dp))
        Switch(
            checked         = checked,
            onCheckedChange = onCheckedChange,
            enabled         = enabled,
            colors          = SwitchDefaults.colors(
                checkedThumbColor        = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor        = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor      = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor      = MaterialTheme.colorScheme.surfaceVariant,
                uncheckedBorderColor     = MaterialTheme.colorScheme.outline,
            ),
        )
    }
}

@Composable
private fun DropdownPreferenceRow(
    icon: ImageVector,
    title: String,
    current: String,
    options: List<Pair<String, String>>,    // key to display label
    onSelect: (String) -> Unit,
    enabled: Boolean = true,
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { expanded = true }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier         = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier           = Modifier.size(20.dp),
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = title,
                style      = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color      = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text  = current,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Icon(
            imageVector        = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint               = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier           = Modifier.size(20.dp),
        )
    }

    DropdownMenu(
        expanded         = expanded,
        onDismissRequest = { expanded = false },
    ) {
        options.forEach { (key, label) ->
            DropdownMenuItem(
                text    = { Text(label) },
                onClick = { onSelect(key); expanded = false },
            )
        }
    }
}

@Preview(showBackground = true, name = "LightMode")
@Composable
fun LightSettingsScreen() {
    EcoFruitTheme (darkTheme = false) {
        SettingsScreen(
            settings = Settings()
        ) { }
    }
}

@Preview(showBackground = true, name = "DarkMode")
@Composable
fun DarkSettingsScreen() {
    EcoFruitTheme(darkTheme = true) {
        SettingsScreen(
            settings = Settings()
        ) { }
    }
}