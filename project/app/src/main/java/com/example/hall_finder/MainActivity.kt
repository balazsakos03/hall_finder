package com.example.hall_finder

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.hall_finder.model.AppLanguage
import com.example.hall_finder.ui.MapScreen
import com.example.hall_finder.ui.QRScreen
import com.example.hall_finder.ui.theme.Hall_finderTheme

class MainActivity : ComponentActivity() {

    private val enableBluetoothLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {  }

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val bluetoothGranted = permissions[Manifest.permission.BLUETOOTH_SCAN] == true ||
                permissions[Manifest.permission.BLUETOOTH_CONNECT] == true
        if (bluetoothGranted) {
            requestEnableBluetooth()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val windowInsetsController = androidx.core.view.WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(androidx.core.view.WindowInsetsCompat.Type.navigationBars())

        requestAllPermissions()

        setContent {
            var appState by remember { mutableStateOf<AppState>(AppState.WaitingForQR) }
            var darkMode by remember { mutableStateOf(false) }
            var currentLanguage by remember { mutableStateOf(AppLanguage.HU) }
            var isAccessibleMode by remember { mutableStateOf(false) }

            Hall_finderTheme(darkTheme = darkMode) {
                when (val state = appState) {
                    is AppState.WaitingForQR -> {
                        QRScreen(
                            onQrScanned = { scannedNodeId ->
                                appState = AppState.MapLoaded(scannedNodeId)
                            },
                            onToggleDarkMode = { darkMode = !darkMode },
                            currentLanguage = currentLanguage,
                            onLanguageChange = { newLanguage -> currentLanguage = newLanguage },
                            isAccessibleMode = isAccessibleMode,
                            onAccessibleModeChange = { isAccessibleMode = it }
                        )
                    }
                    is AppState.MapLoaded -> {
                        MapScreen(
                            startNodeId = state.startNodeId,
                            isDarkMode = darkMode,
                            onToggleDarkMode = { darkMode = !darkMode },
                            currentLanguage = currentLanguage,
                            onBackToMenu = { appState = AppState.WaitingForQR },
                            isAccessibleMode = isAccessibleMode,
                            onAccessibleModeChange = { isAccessibleMode = it }
                        )
                    }
                }
            }
        }
    }

    private fun requestAllPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACTIVITY_RECOGNITION
        )

        // Bluetooth engedélyek - Android 12+ esetén
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        }

        requestPermissionsLauncher.launch(permissions.toTypedArray())
    }

    private fun requestEnableBluetooth() {
        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter
        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enableBluetoothLauncher.launch(enableBtIntent)
        }
    }
}