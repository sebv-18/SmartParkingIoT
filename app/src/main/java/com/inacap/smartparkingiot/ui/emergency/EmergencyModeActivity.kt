package com.inacap.smartparkingiot.ui.emergency

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.inacap.smartparkingiot.data.bluetooth.BluetoothHC05Manager
import com.inacap.smartparkingiot.databinding.ActivityEmergencyModeBinding

class EmergencyModeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmergencyModeBinding
    private lateinit var btManager: BluetoothHC05Manager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmergencyModeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        btManager = BluetoothHC05Manager(this)
        applyFailSecureState(isBarrierOpen = false, sendToBt = false)

        binding.btnForceClose.setOnClickListener {
            applyFailSecureState(isBarrierOpen = false, sendToBt = true)
        }

        binding.btnEmergencyOpen.setOnClickListener {
            applyFailSecureState(isBarrierOpen = true, sendToBt = true)
        }
    }

    private fun applyFailSecureState(isBarrierOpen: Boolean, sendToBt: Boolean) {
        if (!isBarrierOpen) {
            if (sendToBt) {
                Thread { btManager.sendCommand("O") }.start()
            }
            binding.tvFailSecureStatus.text =
                "Estado Fail-Secure: ACTIVO\nBarrera actual: CERRADA (Comando O)"
        } else {
            if (sendToBt) {
                Thread { btManager.sendCommand("A") }.start()
            }
            binding.tvFailSecureStatus.text =
                "Estado Fail-Secure: SUPERVISADO\nBarrera actual: ABIERTA EN EMERGENCIA (Comando A)"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        btManager.disconnect()
    }
}