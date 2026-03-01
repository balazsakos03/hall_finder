package com.example.hall_finder.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun QRScreen(
    onQrScanned: (String) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Text(text = "Scan a QR code to start")

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    // Most szimuláljuk a QR scan-t
                    onQrScanned("n1")
                }
            ) {
                Text("Simulate QR Scan")
            }
        }
    }
}