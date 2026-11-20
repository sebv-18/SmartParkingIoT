package com.inacap.smartparkingiot.data.sync

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket

class WifiSyncManager(private val port: Int = 5055) {

    @Volatile
    private var running = false
    private var serverSocket: ServerSocket? = null

    fun startServer(onMessageReceived: (String) -> Unit, onStatusChanged: (String) -> Unit) {
        stop()
        running = true
        Thread {
            try {
                val server = ServerSocket(port)
                serverSocket = server
                onStatusChanged("Esperando otro celular en puerto $port...")

                while (running) {
                    val client: Socket = server.accept()
                    Thread {
                        try {
                            val reader = BufferedReader(InputStreamReader(client.getInputStream()))
                            val line = reader.readLine()
                            if (line != null) {
                                onMessageReceived(line)
                            }
                            client.close()
                        } catch (_: Exception) {
                        }
                    }.start()
                }
            } catch (e: Exception) {
                if (running) {
                    onStatusChanged("Error servidor Wi-Fi: ${e.message}")
                }
            }
        }.start()
    }

    fun sendState(
        hostIp: String,
        payload: String,
        onResult: (Boolean, String) -> Unit
    ) {
        Thread {
            try {
                Socket(hostIp.trim(), port).use { socket ->
                    val out = PrintWriter(socket.getOutputStream(), true)
                    out.println(payload)
                }
                onResult(true, "Sincronizado con $hostIp")
            } catch (e: Exception) {
                onResult(false, "Error Wi-Fi: ${e.message ?: "sin conexión"}")
            }
        }.start()
    }

    fun stop() {
        running = false
        try {
            serverSocket?.close()
        } catch (_: Exception) {
        }
        serverSocket = null
    }
}