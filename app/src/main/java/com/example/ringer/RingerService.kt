package com.example.ringer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class RingerService : Service() {

    private lateinit var audioManager: AudioManager

    private val ringerReceiver = RingerReceiver()

    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        startForeground(NOTIFICATION_ID, createNotification())
        
        val filter = android.content.IntentFilter(AudioManager.RINGER_MODE_CHANGED_ACTION)
        registerReceiver(ringerReceiver, filter)
        
        ensureRingerMode()
    }

    override fun onDestroy() {
        unregisterReceiver(ringerReceiver)
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ensureRingerMode()
        return START_STICKY
    }

    private fun ensureRingerMode() {
        if (audioManager.ringerMode != AudioManager.RINGER_MODE_NORMAL) {
            try {
                audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
            } catch (e: SecurityException) {
                // Log error or handle missing DND permission
            }
        }
    }

    private fun createNotification(): Notification {
        val channelId = "ringer_service_channel"
        val channel = NotificationChannel(
            channelId,
            "Ringer Service",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Ringer Service Running")
            .setContentText("Keeping phone in ringer mode")
            .setSmallIcon(android.R.drawable.ic_lock_silent_mode_off)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val NOTIFICATION_ID = 1
    }
}
