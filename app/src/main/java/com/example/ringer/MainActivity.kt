package com.example.ringer

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            checkAndStartService()
        } else {
            Toast.makeText(this, "Notification permission required for foreground service", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnStart = findViewById<Button>(R.id.btnStart)
        val btnStop = findViewById<Button>(R.id.btnStop)

        btnStart.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    checkAndStartService()
                }
            } else {
                checkAndStartService()
            }
        }

        btnStop.setOnClickListener {
            stopRingerService()
        }
    }

    private fun checkAndStartService() {
        if (checkNotificationPolicyAccess()) {
            startRingerService()
        } else {
            requestNotificationPolicyAccess()
        }
    }

    private fun checkNotificationPolicyAccess(): Boolean {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return notificationManager.isNotificationPolicyAccessGranted
    }

    private fun requestNotificationPolicyAccess() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
        startActivity(intent)
        Toast.makeText(this, "Please grant Do Not Disturb access", Toast.LENGTH_LONG).show()
    }

    private fun startRingerService() {
        val intent = Intent(this, RingerService::class.java)
        startForegroundService(intent)
        Toast.makeText(this, "Ringer Service Started", Toast.LENGTH_SHORT).show()
    }

    private fun stopRingerService() {
        val intent = Intent(this, RingerService::class.java)
        stopService(intent)
        Toast.makeText(this, "Ringer Service Stopped", Toast.LENGTH_SHORT).show()
    }
}
