package com.example.ringer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build

class RingerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == AudioManager.RINGER_MODE_CHANGED_ACTION || intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            if (audioManager.ringerMode != AudioManager.RINGER_MODE_NORMAL) {
                try {
                    audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
                } catch (e: SecurityException) {
                    val serviceIntent = Intent(context, RingerService::class.java)
                    context.startForegroundService(serviceIntent)
                }
            } else if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
                // Just start the service to ensure it's running after boot
                val serviceIntent = Intent(context, RingerService::class.java)
                context.startForegroundService(serviceIntent)
            }
        }
    }
}
