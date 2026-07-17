package com.joshua.slideremote

import android.accessibilityservice.AccessibilityService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent

class VolumeAccessibilityService : AccessibilityService() {

    private var lastEventTime = 0L
    private val debounceMs = 300L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    override fun onServiceConnected() {
        super.onServiceConnected()
        showPersistentNotification()
    }

    private fun showPersistentNotification() {
        val channelId = "slide_remote_status"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                channelId,
                "Slide Remote Status",
                NotificationManager.IMPORTANCE_LOW
            )
            manager.createNotificationChannel(channel)
        }

        val openAppIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Slide Remote active")
            .setContentText("Volume buttons are controlling your Mac")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(1, notification)
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.action != KeyEvent.ACTION_DOWN) {
            return isVolumeKey(event.keyCode)
        }

        val now = System.currentTimeMillis()
        if (now - lastEventTime < debounceMs) {
            return isVolumeKey(event.keyCode)
        }

        when (event.keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                lastEventTime = now
                SocketManager.sendCommand("next")
                return true
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                lastEventTime = now
                SocketManager.sendCommand("prev")
                return true
            }
        }
        return false
    }

    private fun isVolumeKey(keyCode: Int): Boolean {
        return keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
    }
}
