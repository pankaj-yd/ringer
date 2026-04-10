package com.example.ringer

import android.Manifest
import android.app.ActivityManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.switchmaterial.SwitchMaterial

class MainActivity : AppCompatActivity() {

    private lateinit var serviceToggle: SwitchMaterial

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            checkAndStartService()
        } else {
            serviceToggle.isChecked = false
            Toast.makeText(this, "Notification permission required for foreground service", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        serviceToggle = findViewById(R.id.serviceToggle)
        serviceToggle.isChecked = isServiceRunning()

        serviceToggle.setOnCheckedChangeListener { _, isChecked ->
            saveServiceState(isChecked)
            if (isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        checkAndStartService()
                    }
                } else {
                    checkAndStartService()
                }
            } else {
                stopRingerService()
            }
        }
    }

    private fun saveServiceState(isEnabled: Boolean) {
        val sharedPref = getSharedPreferences("ringer_prefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("service_enabled", isEnabled)
            apply()
        }
    }

    private fun getSavedServiceState(): Boolean {
        val sharedPref = getSharedPreferences("ringer_prefs", Context.MODE_PRIVATE)
        return sharedPref.getBoolean("service_enabled", false)
    }

    override fun onResume() {
        super.onResume()
        serviceToggle.isChecked = isServiceRunning() || getSavedServiceState()
        // If it should be running but isn't (e.g. killed), restart it
        if (getSavedServiceState() && !isServiceRunning()) {
            checkAndStartService()
        }
    }

    private fun isServiceRunning(): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (RingerService::class.java.name == service.service.className) {
                return true
            }
        }
        return false
    }

    private fun checkAndStartService() {
        if (checkNotificationPolicyAccess()) {
            startRingerService()
            saveServiceState(true)
            requestIgnoreBatteryOptimizations()
            showAutoStartSettings()
        } else {
            serviceToggle.isChecked = false
            saveServiceState(false)
            requestNotificationPolicyAccess()
        }
    }

    private fun showAutoStartSettings() {
        val manufacturer = Build.MANUFACTURER.lowercase()
        val intent = Intent()
        try {
            when {
                manufacturer.contains("oppo") || manufacturer.contains("realme") -> {
                    intent.setClassName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")
                    startActivity(intent)
                    Toast.makeText(this, "Please enable 'Auto-start' for Ringer", Toast.LENGTH_LONG).show()
                }
                manufacturer.contains("vivo") -> {
                    intent.setClassName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")
                    startActivity(intent)
                }
                manufacturer.contains("xiaomi") -> {
                    intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")
                    startActivity(intent)
                }
            }
        } catch (e: Exception) {
            // Fallback for newer Oppo/Realme versions or if activity name changed
            try {
                intent.setClassName("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity")
                startActivity(intent)
            } catch (ex: Exception) {
                // Ignore if not found
            }
        }
    }

    private fun requestIgnoreBatteryOptimizations() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = android.net.Uri.parse("package:$packageName")
                }
                startActivity(intent)
            }
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
