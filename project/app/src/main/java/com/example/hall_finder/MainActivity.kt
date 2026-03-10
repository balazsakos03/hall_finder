package com.example.hall_finder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
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
        setContent {
            var appState by remember { mutableStateOf<AppState>(AppState.WaitingForQR) }
            var darkMode by remember { mutableStateOf(false) }

            //globalis nyelvi allapot
            var currentLanguage by remember { mutableStateOf(AppLanguage.HU) }

            Hall_finderTheme(darkTheme = darkMode) {
                when (val state = appState){
                    is AppState.WaitingForQR -> {
                        QRScreen(
                            onQrScanned = {scannedNodeId ->
                                appState = AppState.MapLoaded(scannedNodeId)
                            },
                            onToggleDarkMode = {
                                darkMode = !darkMode
                            },
                            //nyelv
                            currentLanguage = currentLanguage,
                            onLanguageChange = { newLanguage ->
                                currentLanguage = newLanguage
                            }
                        )
                    }
                    is AppState.MapLoaded -> {
                        MapScreen(
                            startNodeId = state.startNodeId,
                            isDarkMode = darkMode,
                            onToggleDarkMode = { darkMode = !darkMode },
                            //nyelv
                            currentLanguage = currentLanguage
                        )
                    }
                }
            }
        }
    }
}