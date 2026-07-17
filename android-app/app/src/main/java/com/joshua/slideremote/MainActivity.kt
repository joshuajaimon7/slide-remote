package com.joshua.slideremote

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val ipInput = findViewById<EditText>(R.id.ipInput)
        val connectButton = findViewById<Button>(R.id.connectButton)
        val statusText = findViewById<TextView>(R.id.statusText)
        val accessibilityButton = findViewById<Button>(R.id.openAccessibilitySettingsButton)

        val prefs = getSharedPreferences("slide_remote", MODE_PRIVATE)
        ipInput.setText(prefs.getString("last_ip", ""))

        SocketManager.onStatusChange = { connected ->
            runOnUiThread {
                statusText.text = if (connected) "Connected ✓" else "Not connected"
            }
        }

        connectButton.setOnClickListener {
            val ip = ipInput.text.toString().trim()
            if (ip.isNotEmpty()) {
                prefs.edit().putString("last_ip", ip).apply()
                statusText.text = "Connecting..."
                SocketManager.connect(ip)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001
                )
            }
        }

        accessibilityButton.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
