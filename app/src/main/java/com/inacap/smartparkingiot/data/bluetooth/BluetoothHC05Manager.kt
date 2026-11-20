package com.inacap.smartparkingiot.data.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import java.io.IOException
import java.io.OutputStream
import java.util.UUID

class BluetoothHC05Manager(context: Context) {

    private val bluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val adapter: BluetoothAdapter? = bluetoothManager.adapter

    private var socket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null

    fun isBluetoothAvailable(): Boolean = adapter != null
    fun isBluetoothEnabled(): Boolean = adapter?.isEnabled == true
    fun isConnected(): Boolean = socket?.isConnected == true && outputStream != null

    @SuppressLint("MissingPermission")
    fun getPairedDevices(): List<BluetoothDevice> {
        return adapter?.bondedDevices?.toList().orEmpty()
    }

    @SuppressLint("MissingPermission")
    fun connectToDevice(device: BluetoothDevice): Result<String> {
        disconnect()
        return try {
            adapter?.cancelDiscovery()
            val sppSocket = device.createRfcommSocketToServiceRecord(SPP_UUID)
            sppSocket.connect()
            socket = sppSocket
            outputStream = sppSocket.outputStream
            Result.success("Conectado a ${device.name ?: device.address}")
        } catch (e: IOException) {
            disconnect()
            Result.failure(e)
        }
    }

    fun sendCommand(command: String): Result<String> {
        val stream = outputStream
        if (!isConnected() || stream == null) {
            return Result.failure(IOException("Bluetooth HC-05 no conectado"))
        }
        return try {
            stream.write(command.toByteArray(Charsets.UTF_8))
            stream.flush()
            Result.success(command)
        } catch (e: IOException) {
            disconnect()
            Result.failure(e)
        }
    }

    fun disconnect() {
        try {
            outputStream?.close()
        } catch (_: IOException) {
        }
        try {
            socket?.close()
        } catch (_: IOException) {
        }
        outputStream = null
        socket = null
    }

    companion object {
        private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }
}