package com.example.ecofruit.ui.services

import android.app.PendingIntent
import android.content.Intent
import android.app.NotificationChannel
import android.app.NotificationManager
import android.media.RingtoneManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.ecofruit.R
import com.example.ecofruit.ui.activities.LauncherActivity
import com.example.ecofruit.ui.data.repository.SettingsRepository
import com.example.ecofruit.ui.data.repository.UserRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch


class MessagingService: FirebaseMessagingService() {
    private val channelId = "ecofruit_channel"
    private val userRepo = UserRepository.getInstance()
    private val settingsRepo by lazy { SettingsRepository.getInstance(applicationContext) }

    override fun onCreate() {
        Log.d(TAG, "  FCM SERVICE INICIALIZADO          ")
        super.onCreate()
    }

    // [START receive_message]
    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        Log.d(TAG, "    MENSAJE FCM RECIBIDO             ")
        Log.d(TAG, "From: ${remoteMessage.from}")

        // Verificar si contiene datos
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Data payload: ${remoteMessage.data}")
        }

        // Verificar si contiene notificación
        remoteMessage.notification?.let { msg ->
            Log.d(TAG, "Notification title: ${msg.title}")
            Log.d(TAG, "Notification body: ${msg.body}")
            CoroutineScope(Dispatchers.IO).launch {
                settingsRepo.settingsFlow.firstOrNull()?.let { settings ->
                    if (settings.notifications) {
                        sendNotification(messageBody = msg.body ?: "", title = msg.title ?: getString(R.string.app_name) )
                    } else {
                        Log.d(TAG, "Notificaciones desactivadas")
                    }
                }

            }
        }
    }
    // [END receive_message]


    // [START on_new_token]
    /**
     * Called if the FCM registration token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the
     * FCM registration token is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String) {
        Log.d(TAG, "    NUEVO TOKEN FCM GENERADO          ")
        Log.d(TAG, "Token: $token")


        // Guardar el token de forma segura
        CoroutineScope(Dispatchers.IO).launch {
            try {
                var producerNotifications = false
                // Obtener el token anterior (sin usar collect que es infinito)
                settingsRepo.settingsFlow.firstOrNull()?.let {
                    it.fcmToken?.let { oldToken ->
                        Log.d(TAG, "Token anterior encontrado: $oldToken")
                        userRepo.updateFcmTokenStatus(oldToken, false)
                        Log.d(TAG, "Token anterior marcado como inactivo")
                    }
                    producerNotifications = it.producersNotification
                }

                // Guardar el nuevo token localmente
                settingsRepo.setFcmToken(token)
                Log.d(TAG, "Token guardado localmente")

                // Guardar el token en el servidor
                userRepo.saveFcmToken(token, producerNotifications)
                Log.d(TAG, "Token guardado en servidor")

            } catch (e: Exception) {
                Log.e(TAG, "Error actualizando token FCM", e)
            }
        }
    }
    // [END on_new_token]

    private fun sendNotification(messageBody: String, title: String) {
        val intent = Intent(this, LauncherActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val requestCode = 0
        val pendingIntent = PendingIntent.getActivity(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE,
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(NotificationManager::class.java)

        if (notificationManager.getNotificationChannel(channelId) == null) {
            val channel = NotificationChannel(
                channelId,
                "EcoFruit Notifications",
                NotificationManager.IMPORTANCE_DEFAULT,
            )
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Canal creado desde sendNotification(): $channelId")
        }

        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notificationBuilder.build())
        Log.d(TAG, "Notificación publicada con ID: $notificationId")
    }

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }
}