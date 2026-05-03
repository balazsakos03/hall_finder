package com.example.hall_finder.checkpoint

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class CheckpointEvent(
    val nodeId: String,
    val rssi: Int,
    val timestamp: Long = System.currentTimeMillis()
)

object CheckpointManager {

    private const val TAG = "CheckpointManager"

    private val knownCheckpoints = mapOf(
        "n4" to "n4"
    )

    private const val RSSI_THRESHOLD = -55

    private val _lastCheckpoint = MutableStateFlow<CheckpointEvent?>(null)
    val lastCheckpoint = _lastCheckpoint.asStateFlow()

    private var bluetoothLeScanner: BluetoothLeScanner? = null
    private var isScanning = false

    private val scanCallback = object : ScanCallback() {
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val deviceName = result.device.name ?: return
            val rssi = result.rssi

            Log.d(TAG, "BLE eszköz: $deviceName, RSSI: $rssi")

            val nodeId = knownCheckpoints[deviceName] ?: return

            if (rssi >= RSSI_THRESHOLD) {
                Log.d(TAG, "CHECKPOINT SNAP! nodeId=$nodeId, rssi=$rssi")
                _lastCheckpoint.value = CheckpointEvent(nodeId = nodeId, rssi = rssi)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(TAG, "BLE scan hiba: $errorCode")
        }
    }

    @SuppressLint("MissingPermission")
    fun startScanning(context: Context) {
        if (isScanning) return

        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            Log.w(TAG, "Bluetooth nincs bekapcsolva")
            return
        }

        bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        bluetoothLeScanner?.startScan(null, settings, scanCallback)
        isScanning = true
        Log.d(TAG, "BLE scan elindult")
    }

    @SuppressLint("MissingPermission")
    fun stopScanning() {
        if (!isScanning) return
        bluetoothLeScanner?.stopScan(scanCallback)
        isScanning = false
        Log.d(TAG, "BLE scan leallitva")
    }

    fun resetLastCheckpoint() {
        _lastCheckpoint.value = null
    }
}