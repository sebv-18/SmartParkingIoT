package com.inacap.smartparkingiot.ui.control

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.inacap.smartparkingiot.data.bluetooth.BluetoothHC05Manager
import com.inacap.smartparkingiot.databinding.ActivityControlManualBinding

class ControlManualActivity : AppCompatActivity() {

    private lateinit var binding: ActivityControlManualBinding
    private lateinit var btManager: BluetoothHC05Manager

    private val bluetoothPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                connectPairedHc05()
            } else {
                binding.tvLastCommand.text = "Permiso BLUETOOTH_CONNECT denegado. Actívalo en ajustes."
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityControlManualBinding.inflate(layoutInflater)
        setContentView(binding.root)

        btManager = BluetoothHC05Manager(this)
        ensureBluetoothPermissionAndConnect()

        binding.btnOpenBarrier.setOnClickListener { sendCommand("A", "Abrir barrera") }
        binding.btnCloseBarrier.setOnClickListener { sendCommand("O", "Cerrar barrera") }
        binding.btnAddVehicle.setOnClickListener { sendCommand("E", "Sumar vehículo") }
        binding.btnSubVehicle.setOnClickListener { sendCommand("U", "Restar vehículo") }
        binding.btnResetCounter.setOnClickListener { sendCommand("I", "Reiniciar contador") }
    }

    private fun ensureBluetoothPermissionAndConnect() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val hasConnect = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasConnect) {
                bluetoothPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
                return
            }
        }
        connectPairedHc05()
    }

    private fun connectPairedHc05() {
        try {
            if (!btManager.isBluetoothAvailable() || !btManager.isBluetoothEnabled()) {
                binding.tvLastCommand.text = "Activa el Bluetooth del teléfono para conectar al HC-05"
                return
            }

            val hc05 = btManager.getPairedDevices().firstOrNull { device ->
                @Suppress("DEPRECATION")
                device.name?.contains("HC-05", ignoreCase = true) == true
            }

            if (hc05 == null) {
                binding.tvLastCommand.text = "Vincula el HC-05 en ajustes Bluetooth del teléfono"
                return
            }

            Thread {
                val result = btManager.connectToDevice(hc05)
                runOnUiThread {
                    result.onSuccess { msg ->
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                    }.onFailure {
                        binding.tvLastCommand.text = "Error al conectar HC-05 (revisa si está encendido)"
                    }
                }
            }.start()
        } catch (e: SecurityException) {
            binding.tvLastCommand.text = "Falta permiso Bluetooth: ${e.message}"
        }
    }

    private fun sendCommand(code: String, label: String) {
        Thread {
            val result = btManager.sendCommand(code)
            runOnUiThread {
                result.onSuccess {
                    binding.tvLastCommand.text = "Enviado por HC-05: $label ($code)"
                }.onFailure {
                    binding.tvLastCommand.text = "Sin conexión HC-05 | Último intento: $label ($code)"
                    Toast.makeText(this, "No conectado al HC-05", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        btManager.disconnect()
    }
}