package com.example.hall_finder.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Language
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.hall_finder.model.AppLanguage
import com.example.hall_finder.model.Translations

@Composable
fun QRScreen(
    onQrScanned: (String) -> Unit,
    onToggleDarkMode: () -> Unit,
    currentLanguage: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit
) {
    val primary   = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary

    //pulzalo animacio
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue  = 1.06f,
        animationSpec = infiniteRepeatable(
            animation  = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue  = 0.9f,
        animationSpec = infiniteRepeatable(
            animation  = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    var languageMenuExpanded by remember { mutableStateOf(false) }

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
        //nyelv valaszto menu
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

            //qr scanner ikon keret
            Box(contentAlignment = Alignment.Center) {
                //kulso pulzalo gyuru
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .scale(pulseScale)
                        .clip(RoundedCornerShape(36.dp))
                        .background(primary.copy(alpha = pulseAlpha * 0.12f))
                        .border(
                            width = 2.dp,
                            color = primary.copy(alpha = pulseAlpha * 0.5f),
                            shape = RoundedCornerShape(36.dp)
                        )
                )
                //belso kor
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    primary.copy(alpha = 0.2f),
                                    primary.copy(alpha = 0.08f)
                                )
                            )
                        )
                        .border(
                            width = 1.5f.dp,
                            color = primary.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(28.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        tint     = primary,
                        modifier = Modifier.size(60.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            //szovegek
            Text(
                text       = Translations.qrTitle(currentLanguage),
                style      = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign  = TextAlign.Center,
                color      = MaterialTheme.colorScheme.onSurface,
                lineHeight = 32.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text      = Translations.qrSubtitle(currentLanguage),
                style     = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            //scan gomb
            Button(
                onClick = { onQrScanned("n1") },
                modifier = Modifier
                    .fillMaxWidth(0.72f)
                    .height(54.dp),
                shape  = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text       = Translations.qrScanBtn(currentLanguage),
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            //demo gomb
            OutlinedButton(
                onClick  = { onQrScanned("n1") },
                modifier = Modifier
                    .fillMaxWidth(0.72f)
                    .height(48.dp),
                shape    = RoundedCornerShape(18.dp)
            ) {
                Text(
                    text     = Translations.qrDemoBtn(currentLanguage),
                    fontSize = 14.sp,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}