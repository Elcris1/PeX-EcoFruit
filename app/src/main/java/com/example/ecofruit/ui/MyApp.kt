package com.example.ecofruit.ui

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.example.ecofruit.ui.data.repository.SettingsRepository
import com.example.ecofruit.ui.data.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class MyApp: Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

        createNotificationChannel()

        val settingsRepo = SettingsRepository.getInstance(this)
        val userRepo = UserRepository.getInstance()

        // Obtener el token FCM existente o generar uno nuevo
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d(TAG, "    FCM TOKEN OBTENIDO EN INIT       ")
                Log.d(TAG, "Token: $token")
                
                // Guardar el token en SettingsRepository
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        settingsRepo.settingsFlow.firstOrNull()?.let { it ->
                            val savedToken = it.fcmToken
                            if (savedToken != null && savedToken != token) {
                                userRepo.updateFcmTokenStatus(savedToken, false)
                                userRepo.saveFcmToken(token, it.producersNotification)
                           }
                            settingsRepo.setFcmToken(token)
                            Log.d(TAG, "Token guardado en SettingsRepository")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error guardando token en SettingsRepository", e)
                    }
                }
            } else {
                Log.e(TAG, "Error obteniendo token FCM", task.exception)
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                FCM_CHANNEL_ID,
                "EcoFruit Notifications",
                NotificationManager.IMPORTANCE_DEFAULT,
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Canal FCM creado: $FCM_CHANNEL_ID")
        }
    }

    companion object {
        private const val TAG = "MyApp"
        private const val FCM_CHANNEL_ID = "ecofruit_channel"
    }
}