package com.joshua.slideremote

import android.accessibilityservice.AccessibilityService
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent

/**
 * Intercepts volume up/down key events system-wide and forwards them to the
 * Mac as "next"/"prev" commands, instead of letting them change media volume.
 *
 * Requires the user to manually enable this service under:
 * Settings > Accessibility > Installed apps > Slide Remote
 * (Android does not allow enabling AccessibilityServices programmatically.)
 */
class VolumeAccessibilityService : AccessibilityService() {

    // Debounce so a single physical press (down + up events) doesn't fire twice
    private var lastEventTime = 0L
    private val debounceMs = 300L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Required override, not used for key interception — key events come
        // through onKeyEvent below instead.
    }

    override fun onInterrupt() {}

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.action != KeyEvent.ACTION_DOWN) {
            // Let ACTION_UP pass through untouched; only act on the press itself
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
                return true // consume the event so system volume doesn't change
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
