package com.example.ringer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build

class RingerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val sharedPref = context.getSharedPreferences("ringer_prefs", Context.MODE_PRIVATE)
        val isEnabled = sharedPref.getBoolean("service_enabled", false)

        if (!isEnabled) return

        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED || 
            action == Intent.ACTION_LOCKED_BOOT_COMPLETED || 
            action == "android.intent.action.QUICKBOOT_POWERON" || 
            action == "com.htc.intent.action.QUICKBOOT_POWERON") {
            
            val serviceIntent = Intent(context, RingerService::class.java)
            context.startForegroundService(serviceIntent)
        } else if (action == AudioManager.RINGER_MODE_CHANGED_ACTION) {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            if (audioManager.ringerMode != AudioManager.RINGER_MODE_NORMAL) {
                try {
                    audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
                } catch (e: SecurityException) {
                    // If we can't change it here, ensure the service is running to handle it
                    val serviceIntent = Intent(context, RingerService::class.java)
                    context.startForegroundService(serviceIntent)
                }
            }
        }
    }
}
