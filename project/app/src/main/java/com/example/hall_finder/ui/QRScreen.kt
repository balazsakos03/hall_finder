package com.example.hall_finder.ui

import android.Manifest
import android.content.pm.PackageManager
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.hall_finder.model.AppLanguage
import com.example.hall_finder.model.Translations
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

@Composable
fun QRScreen(
    onQrScanned: (String) -> Unit,
    onToggleDarkMode: () -> Unit,
    currentLanguage: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val primary = MaterialTheme.colorScheme.primary

    var isScanning by remember { mutableStateOf(false) }
    var languageMenuExpanded by remember { mutableStateOf(false) }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
            if (granted) {
                isScanning = true // Ha megadta, egyből indul is a kamera!
            }
        }
    )

    if (isScanning && hasCameraPermission) {
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val barcodeScanner = BarcodeScanning.getClient(
                            BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build()
                        )

                        val imageAnalysis = ImageAnalysis.Builder()
                            .setTargetResolution(Size(1280, 720))
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also { analysis ->
                                analysis.setAnalyzer(ContextCompat.getMainExecutor(ctx)) { imageProxy ->
                                    processImageProxy(barcodeScanner, imageProxy) { scannedValue ->
                                        // TALÁLAT! Kilépünk a kamerából és átadjuk az adatot.
                                        isScanning = false
                                        onQrScanned(scannedValue)
                                    }
                                }
                            }

                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                imageAnalysis
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)))
            Box(
                modifier = Modifier
                    .size(250.dp)
                    .align(Alignment.Center)
                    .border(3.dp, Color.White, RoundedCornerShape(24.dp))
                    .background(Color.Transparent)
            )

            FilledIconButton(
                onClick = { isScanning = false },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 48.dp, start = 24.dp)
                    .size(48.dp),
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
            ) {
                Icon(Icons.Default.Close, contentDescription = "Mégse", tint = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
    else {
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 1f, targetValue = 1.06f,
            animationSpec = infiniteRepeatable(animation = tween(900, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse),
            label = "pulseScale"
        )
        val pulseAlpha by infiniteTransition.animateFloat(
            initialValue = 0.4f, targetValue = 0.9f,
            animationSpec = infiniteRepeatable(animation = tween(900, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse),
            label = "pulseAlpha"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                )
        ) {
            // Nyelv választó menü
            Box(modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)) {
                FilledTonalIconButton(onClick = { languageMenuExpanded = true }) {
                    Icon(Icons.Default.Language, contentDescription = "Nyelv / Language")
                }
                DropdownMenu(
                    expanded = languageMenuExpanded,
                    onDismissRequest = { languageMenuExpanded = false }
                ) {
                    AppLanguage.values().forEach { language ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = language.displayName,
                                    fontWeight = if (currentLanguage == language) FontWeight.Bold else FontWeight.Normal,
                                    color = if (currentLanguage == language) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            onClick = {
                                onLanguageChange(language)
                                languageMenuExpanded = false
                            }
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // QR scanner ikon keret
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .scale(pulseScale)
                            .clip(RoundedCornerShape(36.dp))
                            .background(primary.copy(alpha = pulseAlpha * 0.12f))
                            .border(width = 2.dp, color = primary.copy(alpha = pulseAlpha * 0.5f), shape = RoundedCornerShape(36.dp))
                    )
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(brush = Brush.radialGradient(colors = listOf(primary.copy(alpha = 0.2f), primary.copy(alpha = 0.08f))))
                            .border(width = 1.5f.dp, color = primary.copy(alpha = 0.4f), shape = RoundedCornerShape(28.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = null, tint = primary, modifier = Modifier.size(60.dp))
                    }
                }

                Spacer(modifier = Modifier.height(36.dp))

                Text(
                    text = Translations.qrTitle(currentLanguage),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 32.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = Translations.qrSubtitle(currentLanguage),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(48.dp))

                // SCAN GOMB (Itt nyitjuk meg a kamerát!)
                Button(
                    onClick = {
                        if (hasCameraPermission) {
                            isScanning = true
                        } else {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(0.72f).height(54.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primary)
                ) {
                    Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = Translations.qrScanBtn(currentLanguage), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // DEMO GOMB (Ez továbbra is azonnal bevisz a térképre kódolvasás nélkül)
                OutlinedButton(
                    onClick = { onQrScanned("n1") },
                    modifier = Modifier.fillMaxWidth(0.72f).height(48.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(text = Translations.qrDemoBtn(currentLanguage), fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
private fun processImageProxy(
    barcodeScanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    imageProxy: ImageProxy,
    onSuccess: (String) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        barcodeScanner.process(image)
            .addOnSuccessListener { barcodes ->
                barcodes.firstOrNull()?.rawValue?.let { value -> onSuccess(value) }
            }
            .addOnCompleteListener { imageProxy.close() }
    } else {
        imageProxy.close()
    }
}