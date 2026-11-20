package com.inacap.smartparkingiot.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.inacap.smartparkingiot.data.model.ParkingState
import com.inacap.smartparkingiot.data.sync.WifiSyncManager
import com.inacap.smartparkingiot.databinding.ActivityDashboardBinding
import com.inacap.smartparkingiot.ui.control.ControlManualActivity
import com.inacap.smartparkingiot.ui.emergency.EmergencyModeActivity

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var wifiSyncManager: WifiSyncManager
    private var state = ParkingState()
    private var syncStatusText = "Inactiva"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        wifiSyncManager = WifiSyncManager()
        renderState()
        startWifiSyncServer()

        binding.btnSyncNow.setOnClickListener {
            val peerIp = binding.etPeerIp.text?.toString()?.trim().orEmpty()
            if (peerIp.isEmpty()) {
                Toast.makeText(this, "Ingresa la IP del otro celular", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val payload = "OCCUPIED:${state.occupiedSpots}|EMERGENCY:${state.isEmergencyMode}"
            wifiSyncManager.sendState(peerIp, payload) { ok, msg ->
                runOnUiThread {
                    syncStatusText = msg
                    state = state.copy(isSyncConnected = ok)
                    renderState()
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnGoManual.setOnClickListener {
            startActivity(Intent(this, ControlManualActivity::class.java))
        }

        binding.btnGoEmergency.setOnClickListener {
            startActivity(Intent(this, EmergencyModeActivity::class.java))
        }
    }

    private fun startWifiSyncServer() {
        wifiSyncManager.startServer(
            onMessageReceived = { raw ->
                runOnUiThread {
                    applyRemotePayload(raw)
                }
            },
            onStatusChanged = { status ->
                runOnUiThread {
                    syncStatusText = status
                    renderState()
                }
            }
        )
    }

    private fun applyRemotePayload(raw: String) {
        val parts = raw.trim().split("|")
        var newOccupied = state.occupiedSpots
        var newEmergency = state.isEmergencyMode

        for (part in parts) {
            when {
                part.startsWith("OCCUPIED:") -> {
                    newOccupied = part.removePrefix("OCCUPIED:").toIntOrNull()
                        ?.coerceIn(0, state.maxCapacity) ?: newOccupied
                }
                part.startsWith("EMERGENCY:") -> {
                    newEmergency = part.removePrefix("EMERGENCY:").toBooleanStrictOrNull()
                        ?: newEmergency
                }
            }
        }

        state = state.copy(
            occupiedSpots = newOccupied,
            isEmergencyMode = newEmergency,
            isSyncConnected = true
        )
        syncStatusText = "Recibido: $raw"
        renderState()
    }

    private fun renderState() {
        val btText = if (state.isBluetoothConnected) "Conectado" else "Desconectado"
        val emergencyText = if (state.isEmergencyMode) "ACTIVO (Fail-Secure)" else "Normal"
        binding.tvStatusInfo.text = """
            Capacidad máxima: ${state.maxCapacity}
            Vehículos registrados: ${state.occupiedSpots}
            Espacios disponibles: ${state.availableSpots}
            Modo: $emergencyText
            Bluetooth HC-05: $btText
            Sincronización: $syncStatusText
        """.trimIndent()
    }

    override fun onDestroy() {
        super.onDestroy()
        wifiSyncManager.stop()
    }
}