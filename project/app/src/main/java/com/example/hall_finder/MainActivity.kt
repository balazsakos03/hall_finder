package com.example.hall_finder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.hall_finder.model.AppLanguage
import com.example.hall_finder.ui.MapScreen
import com.example.hall_finder.ui.QRScreen
import com.example.hall_finder.ui.theme.Hall_finderTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val windowInsetsController = androidx.core.view.WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(androidx.core.view.WindowInsetsCompat.Type.navigationBars())

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
}