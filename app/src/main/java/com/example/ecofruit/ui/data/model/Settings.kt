package com.example.ecofruit.ui.data.model

data class Settings(
    // Network
    val wifiOnlyMode:       Boolean = false,
    val dataSaver:          Boolean = false,
    val autoSync:           Boolean = true,
    val syncFrequency:      String  = "30min",
    val connectionTimeout:  Int     = 20,
    val maxCacheSizeMb:     Int     = 100,
    val offlineMode:        Boolean = false,
    val preloadImages:      Boolean = true,

    // General
    val darkTheme:          Boolean = false,
    val notifications:      Boolean = true,
    val language:           String  = "es",

    // Messaging
    val fcmToken:           String? = null,
)