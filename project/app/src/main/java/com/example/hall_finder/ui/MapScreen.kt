package com.example.hall_finder.ui

import android.R.attr.rotation
import android.annotation.SuppressLint
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.hall_finder.R
import com.example.hall_finder.graph.AStar
import com.example.hall_finder.model.MapData
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight.Companion.SemiBold
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.atan2

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun MapScreen(startNodeId: String, isDarkMode: Boolean, onToggleDarkMode: () -> Unit) {

    val destinations = listOf(
        "n7" to "Büfé",
        "n8" to "I. Iroda",
        "n9" to "II. Iroda",
        "n10" to "Titkárság",
        "n11" to "III. Iroda",
        "n12" to "IV. Iroda",
        "n13" to "II. Raktár",
        "n14" to "I. Raktár",
        "n15" to "Admin",
        "n16" to "Férfi mosdó",
        "n17" to "Női mosdó"
    )

    val selectedDestination = remember { mutableStateOf(destinations.first()) }
    val pathState = remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(startNodeId, selectedDestination.value.first) {
        val aStar = AStar(MapData.graph, MapData.nodes)
        pathState.value = aStar.findPath(
            startNodeId,
            selectedDestination.value.first
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // map+route layer
        MapContent(
            startNodeId = startNodeId,
            goalNodeId = selectedDestination.value.first,
            path = pathState.value,
            isDarkMode = isDarkMode
        )

        // uticel kivalasztasa
        DestinationCard(
            destinations = destinations,
            selected = selectedDestination.value,
            onSelected = { selectedDestination.value = it },
            onToggleDarkMode = onToggleDarkMode,
            isDarkMode = isDarkMode,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 52.dp)
        )
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun MapContent(
    startNodeId: String,
    goalNodeId: String,
    path: List<String>,
    isDarkMode: Boolean
) {
    val figmaWidth = 1080f
    val figmaHeight = 1920f

    val infiniteTransition = rememberInfiniteTransition(label="route")
    val dashPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 60f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dashPhase"
    )

    val arrowScale by animateFloatAsState(
        targetValue = if(path.isNotEmpty()) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "arrowScale"
    )

    val primaryColor   = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor  = MaterialTheme.colorScheme.tertiary

    //coroutine scope az animaciok futtatasahoz
    val coroutineScope = rememberCoroutineScope()

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = constraints.maxWidth.toFloat()
        val screenHeight = constraints.maxHeight.toFloat()
        val screenCenterX = screenWidth / 2f
        val screenCenterY = screenHeight / 2f

        val imageAspect = figmaWidth / figmaHeight
        val screenAspect = screenWidth / screenHeight

        val scale: Float
        val offsetX: Float
        val offsetY: Float

        if(screenAspect > imageAspect){
            scale = screenHeight / figmaHeight
            offsetX = (screenWidth - figmaWidth * scale) / 2f
            offsetY = 0f
        } else {
            scale = screenWidth / figmaWidth
            offsetX = 0f
            offsetY = (screenHeight - figmaHeight * scale) / 2f
        }

        val startNode = MapData.nodes.first { it.id == startNodeId }
        val startScreenX = offsetX + startNode.x * scale
        val startScreenY = offsetY + startNode.y * scale

        val arrowAngle = remember(path) {
            if(path.size >= 2){
                val from = MapData.nodes.first {it.id == path[0]}
                val to = MapData.nodes.first {it.id == path[1]}
                val dx = to.x - from.x
                val dy = to.y - from.y
                Math.toDegrees(kotlin.math.atan2(dy.toDouble(), dx.toDouble())).toFloat() + 90f
            } else 0f
        }

        // animatable allapotok
        val zoomScale = remember { Animatable(2f) }
        val mapRotation = remember { Animatable(0f) }
        val panX = remember { Animatable(0f) }
        val panY = remember { Animatable(0f) }

        var isInitialized by remember { mutableStateOf(false) }
        var lastStartNode by remember { mutableStateOf(startNodeId) }

        //re-center funkcio animacioval
        val performRecenter = {
            val dx = startScreenX - screenCenterX
            val dy = startScreenY - screenCenterY

            val rad = Math.toRadians((-arrowAngle).toDouble())
            val cos = kotlin.math.cos(rad).toFloat()
            val sin = kotlin.math.sin(rad).toFloat()

            val sx = dx * 2f
            val sy = dy * 2f

            //eltolas a kepernyo also resze fele
            val yOffset = screenHeight * 0.3f

            val targetPanX = -(sx * cos - sy * sin)
            val targetPanY = -(sx * sin + sy * cos) + yOffset

            //kiszamolom a legrovidebb forgatasi utat
            val currentRot = mapRotation.value
            val targetRotRaw = -arrowAngle
            val diff = (targetRotRaw - currentRot) % 360f
            val normalizedDiff = if (diff > 180f) diff - 360f else if (diff < -180f) diff + 360f else diff
            val finalTargetRot = currentRot + normalizedDiff

            //osszes animacio elinditasa egyszerre
            coroutineScope.launch {
                val animSpec = tween<Float>(durationMillis = 800, easing = FastOutSlowInEasing)
                launch { zoomScale.animateTo(2f, animationSpec = animSpec) }
                launch { mapRotation.animateTo(finalTargetRot, animationSpec = animSpec) }
                launch { panX.animateTo(targetPanX, animationSpec = animSpec) }
                launch { panY.animateTo(targetPanY, animationSpec = animSpec) }
            }
        }

        //automatikus igazitas betolteskor vagy uj kezdopontnal
        if (!isInitialized || startNodeId != lastStartNode) {
            //betolteskor azonnal ugrassal allitom be
            LaunchedEffect(startNodeId) {
                val dx = startScreenX - screenCenterX
                val dy = startScreenY - screenCenterY
                val rad = Math.toRadians((-arrowAngle).toDouble())
                val cos = kotlin.math.cos(rad).toFloat()
                val sin = kotlin.math.sin(rad).toFloat()
                val sx = dx * 2f
                val sy = dy * 2f

                val yOffset = screenHeight * 0.3f

                zoomScale.snapTo(2f)
                mapRotation.snapTo(-arrowAngle)
                panX.snapTo(-(sx * cos - sy * sin))
                panY.snapTo(-(sx * sin + sy * cos) + yOffset)

                isInitialized = true
                lastStartNode = startNodeId
            }
        }

        val mapBgColor = if (isDarkMode){
            Color(0xFF121212)
        }else{
            Color(0xFFFFFFFF)
        }

        //kulso box a gesztusok fogadasaert
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(mapBgColor)
                .pointerInput(Unit) {
                    detectTransformGestures { centroid, pan, zoom, rotation ->
                        //gesztusoknal coroutine ami azonnal vegrahajtodik
                        coroutineScope.launch {
                            val oldScale = zoomScale.value
                            val newScale = (oldScale * zoom).coerceIn(1f, 5f)
                            val effectiveZoom = newScale / oldScale

                            val bx = screenCenterX + panX.value
                            val by = screenCenterY + panY.value

                            val dx = bx - centroid.x
                            val dy = by - centroid.y

                            val sx = dx * effectiveZoom
                            val sy = dy * effectiveZoom

                            val rad = Math.toRadians(rotation.toDouble())
                            val cos = kotlin.math.cos(rad).toFloat()
                            val sin = kotlin.math.sin(rad).toFloat()

                            val rx = sx * cos - sy * sin
                            val ry = sx * sin + sy * cos

                            val newBx = centroid.x + rx + pan.x
                            val newBy = centroid.y + ry + pan.y

                            //a felhasznaloi interakcio azonnal frissiti a nezetet
                            launch { zoomScale.snapTo(newScale) }
                            launch { panX.snapTo(newBx - screenCenterX) }
                            launch { panY.snapTo(newBy - screenCenterY) }
                            launch { mapRotation.snapTo(mapRotation.value + rotation) }
                        }
                    }
                }
        ) {
            //belso box a megjelenitesert az animatable ertekekkel
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = zoomScale.value,
                        scaleY = zoomScale.value,
                        translationX = panX.value,
                        translationY = panY.value,
                        rotationZ = mapRotation.value
                    )
            ) {
                Image(
                    painter = painterResource(
                        id = if (isDarkMode) R.drawable.map_vector_dark else R.drawable.map_vector
                    ),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )

                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (path.size > 1) {
                        for (i in 0 until path.size - 1) {
                            val from = MapData.nodes.first { it.id == path[i] }
                            val to = MapData.nodes.first { it.id == path[i + 1] }

                            val start = Offset(offsetX + from.x * scale, offsetY + from.y * scale)
                            val end   = Offset(offsetX + to.x   * scale, offsetY + to.y   * scale)

                            drawLine(
                                color = primaryColor.copy(alpha = 0.18f),
                                start = start, end = end, strokeWidth = 36f, cap = StrokeCap.Round
                            )
                            drawLine(
                                color = primaryColor.copy(alpha = 0.55f),
                                start = start, end = end, strokeWidth = 14f, cap = StrokeCap.Round
                            )
                            drawLine(
                                color = Color.White.copy(alpha = 0.75f),
                                start = start, end = end, strokeWidth = 14f, cap = StrokeCap.Round,
                                pathEffect = PathEffect.dashPathEffect(
                                    intervals = floatArrayOf(20f, 40f), phase = -dashPhase
                                )
                            )
                        }
                    }

                    val goalNode = MapData.nodes.first { it.id == goalNodeId }
                    drawPinMarker(
                        center = Offset(offsetX + goalNode.x * scale, offsetY + goalNode.y * scale),
                        color  = tertiaryColor,
                        shadowColor = tertiaryColor.copy(alpha = 0.3f),
                        scale  = scale
                    )

                    val startCenter = Offset(
                        offsetX + startNode.x * scale, offsetY + startNode.y * scale
                    )
                    drawNavigationArrow(
                        center = startCenter,
                        angleDeg = arrowAngle,
                        color = secondaryColor,
                        shadowColor = secondaryColor.copy(alpha = 0.35f),
                        arrowScale = arrowScale
                    )
                }
            }
        }

        //re-center gomb
        FloatingActionButton(
            onClick = { performRecenter() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 48.dp),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = "Középre igazítás"
            )
        }
    }
}

//rajzolas segito fuggvenyek
private fun DrawScope.drawNavigationArrow(
    center: Offset,
    angleDeg: Float,
    color: Color,
    shadowColor: Color,
    arrowScale: Float
){
    val r = 32f * arrowScale //alap sugar meret
    rotate(degrees = angleDeg, pivot = center){
        //arnyek
        drawCircle(color = shadowColor, radius = r * 1.9f, center = center)
        //feher keret
        drawCircle(color = Color.White, radius = r * 1.35f, center = center)

        //nyil haromszog(felfele mutat majd forgatja)
        val path = Path().apply {
            moveTo(center.x, center.y - r * 1.3f) //csucs
            lineTo(center.x + r, center.y + r * 0.9f) //jobb also
            lineTo(center.x, center.y + r * 0.3f) //also kozep
            lineTo(center.x - r, center.y + r * 0.9f) //bal also
            close()
        }
        drawPath(path = path, color = color)

        //belso kor(pupilla szeru megjelenes)
        drawCircle(
            color = Color.White.copy(alpha = 0.45f),
            radius = r * 0.35f,
            center = Offset(center.x, center.y + r * 0.1f)
        )
    }
}

private fun DrawScope.drawPinMarker(
    center: Offset,
    color: Color,
    shadowColor: Color,
    scale: Float
){
    val r = 28f
    val stemH = r * 1.4f

    //arnyek
    drawCircle(
        color = shadowColor,
        radius = r * 1.5f,
        center = Offset(center.x, center.y - stemH - r * 0.5f)
    )

    val pinPath = Path().apply{
        addOval(
            androidx.compose.ui.geometry.Rect(
                left = center.x - r,
                top = center.y - stemH - r * 2f,
                right = center.x + r,
                bottom = center.y - stemH
            )
        )
        //haromszog
        moveTo(center.x - r * 0.55f, center.y - stemH - r * 0.3f)
        lineTo(center.x + r * 0.55f, center.y - stemH - r * 0.3f)
        lineTo(center.x,             center.y)   // hegy = center
        close()
    }
    drawPath(path = pinPath, color = color)
    drawCircle(
        color = Color.White,
        radius = r * 0.5f,
        center = Offset(center.x, center.y - stemH - r)
    )
}

@Composable
fun DestinationCard(
    destinations: List<Pair<String, String>>,
    selected: Pair<String, String>,
    onSelected: (Pair<String, String>) -> Unit,
    onToggleDarkMode: () -> Unit,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth(0.92f),
        shape     = RoundedCornerShape(28.dp),
        color     = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // PIN ikon bal oldalon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint   = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Destination szöveg + dropdown
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = "Úti cél",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text     = selected.second,
                    style    = MaterialTheme.typography.titleMedium,
                    fontWeight = SemiBold,
                    modifier = Modifier.clickable { expanded = true }
                )

                DropdownMenu(
                    expanded          = expanded,
                    onDismissRequest  = { expanded = false }
                ) {
                    destinations.forEach { dest ->
                        DropdownMenuItem(
                            text    = { Text(dest.second) },
                            onClick = {
                                onSelected(dest)
                                expanded = false
                            },
                            leadingIcon = {
                                if (dest == selected) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        )
                    }
                }
            }

            // Dark mode toggle gomb
            FilledIconButton(
                onClick = onToggleDarkMode,
                colors  = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = if (isDarkMode) "Világos mód" else "Sötét mód",
                    tint     = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}