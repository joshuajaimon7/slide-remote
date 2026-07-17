package com.joshua.slideremote

import android.util.Log
import okhttp3.*
import java.util.concurrent.TimeUnit

/**
 * Simple singleton WebSocket client. MainActivity calls connect() with the
 * Mac's IP; VolumeAccessibilityService calls sendCommand() whenever a volume
 * key is pressed. Kept as a singleton object so both components share one
 * live connection without needing bound services / IPC.
 */
object SocketManager {

    private const val TAG = "SlideRemote"
    private const val PORT = 8765

    private var client: OkHttpClient? = null
    private var webSocket: WebSocket? = null
    private var currentIp: String? = null

    var isConnected: Boolean = false
        private set

    var onStatusChange: ((Boolean) -> Unit)? = null

    fun connect(ip: String) {
        currentIp = ip
        client = OkHttpClient.Builder()
            .pingInterval(10, TimeUnit.SECONDS) // keep NAT/firewall connection alive
            .build()

        val request = Request.Builder()
            .url("ws://$ip:$PORT")
            .build()

        webSocket = client!!.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                Log.d(TAG, "Connected to $ip")
                isConnected = true
                onStatusChange?.invoke(true)
            }

            override fun onMessage(ws: WebSocket, text: String) {
                Log.d(TAG, "Server says: $text")
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "Connection failed: ${t.message}")
                isConnected = false
                onStatusChange?.invoke(false)
            }

            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "Connection closed: $reason")
                isConnected = false
                onStatusChange?.invoke(false)
            }
        })
    }

    fun sendCommand(command: String) {
        if (isConnected) {
            webSocket?.send(command)
        } else {
            Log.w(TAG, "Not connected, dropping command: $command")
            // Try to reconnect once using the last known IP
            currentIp?.let { connect(it) }
        }
    }

    fun disconnect() {
        webSocket?.close(1000, "App closing")
        isConnected = false
    }
}
