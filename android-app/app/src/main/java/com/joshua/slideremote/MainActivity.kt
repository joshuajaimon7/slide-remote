package com.joshua.slideremote

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val ipInput = findViewById<EditText>(R.id.ipInput)
        val connectButton = findViewById<Button>(R.id.connectButton)
        val statusText = findViewById<TextView>(R.id.statusText)
        val accessibilityButton = findViewById<Button>(R.id.openAccessibilitySettingsButton)

        // Remember last-used IP so you don't retype it every launch
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

        // Android requires manually enabling accessibility services in Settings —
        // this just jumps the user straight there instead of making them hunt for it.
        accessibilityButton.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Don't disconnect here — the accessibility service needs the socket
        // to stay alive even after MainActivity closes.
    }
}
