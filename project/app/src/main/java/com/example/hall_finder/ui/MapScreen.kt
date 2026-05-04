package com.example.hall_finder.ui

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.animateColorAsState
import androidx.compose.material.icons.filled.Straight
import androidx.compose.material.icons.filled.TurnLeft
import androidx.compose.material.icons.filled.TurnRight
import androidx.compose.material.icons.filled.TurnSlightLeft
import androidx.compose.material.icons.filled.TurnSlightRight
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Elevator
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stairs
import androidx.compose.material.icons.filled.Wc
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.SemiBold
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.hall_finder.R
import com.example.hall_finder.checkpoint.CheckpointManager
import com.example.hall_finder.graph.AStar
import com.example.hall_finder.model.AppLanguage
import com.example.hall_finder.model.MapData
import com.example.hall_finder.model.Node
import com.example.hall_finder.model.Translations
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private val STAIR_NODE_IDS    = setOf("n18", "n19", "n21", "n22")
private val ELEVATOR_NODE_IDS = setOf("n20", "n23")

enum class TurnDirection { STRAIGHT, SLIGHT_LEFT, LEFT, SLIGHT_RIGHT, RIGHT }

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun MapScreen(
    startNodeId: String,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    currentLanguage: AppLanguage,
    onBackToMenu: () -> Unit,
    isAccessibleMode: Boolean,
    onAccessibleModeChange: (Boolean) -> Unit
) {
    val context = LocalContext.current

    // BLE scan indítása/leállítása
    DisposableEffect(Unit) {
        CheckpointManager.startScanning(context)
        onDispose { CheckpointManager.stopScanning() }
    }

    val destinations = remember(currentLanguage) {
        Translations.getDestinations(currentLanguage)
    }
    val selectedDestinationId = remember { mutableStateOf(destinations.first().first) }
    val currentSelectedPair   = destinations.firstOrNull { it.first == selectedDestinationId.value } ?: destinations.first()
    val pathState             = remember { mutableStateOf<List<String>>(emptyList()) }
    // Ha checkpoint snap történik és az nincs az útvonalon, innen újraszámítjuk
    val pathStartOverride     = remember { mutableStateOf<String?>(null) }

    LaunchedEffect(startNodeId, selectedDestinationId.value, isAccessibleMode, pathStartOverride.value) {
        val from = pathStartOverride.value ?: startNodeId
        pathState.value = AStar(MapData.graph, MapData.nodes)
            .findPath(from, selectedDestinationId.value, accessibleOnly = isAccessibleMode)
    }

    // Menetidő becslés - az útvonal hosszából számítjuk
    // Pixel -> méter konverzió: a térkép 1080px széles, ez kb. 50 méteres folyosót fed le
    val pixelsPerMeter = 1080f / 50f
    val walkingSpeedMs = 1.4f // átlagos gyaloglási sebesség m/s

    val estimatedSeconds = remember(pathState.value) {
        if (pathState.value.size < 2) return@remember 0
        var totalPixels = 0f
        for (i in 0 until pathState.value.size - 1) {
            val from = MapData.nodes.firstOrNull { it.id == pathState.value[i] } ?: continue
            val to   = MapData.nodes.firstOrNull { it.id == pathState.value[i + 1] } ?: continue
            val dx = to.x - from.x; val dy = to.y - from.y
            totalPixels += kotlin.math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
        }
        val meters = totalPixels / pixelsPerMeter
        (meters / walkingSpeedMs).toInt()
    }

    var currentTurnDirection by remember { mutableStateOf(TurnDirection.STRAIGHT) }

    Box(modifier = Modifier.fillMaxSize()) {
        MapContent(
            startNodeId = startNodeId, goalNodeId = selectedDestinationId.value,
            path = pathState.value, isDarkMode = isDarkMode, currentLanguage = currentLanguage,
            onCheckpointRepath = { checkpointNodeId ->
                pathStartOverride.value = checkpointNodeId
            },
            onTurnDirectionChanged = { currentTurnDirection = it }
        )
        DestinationCard(
            destinations = destinations, selected = currentSelectedPair,
            onSelected = { selectedDestinationId.value = it.first },
            onToggleDarkMode = onToggleDarkMode, isDarkMode = isDarkMode,
            currentLanguage = currentLanguage,
            isAccessibleMode = isAccessibleMode, onAccessibleModeChange = onAccessibleModeChange,
            estimatedSeconds = estimatedSeconds,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 52.dp)
        )

        // Fordulási panel - jobb oldalon, a DestinationCard alatt
        if (pathState.value.isNotEmpty()) {
            TurnPanel(
                direction = currentTurnDirection,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 160.dp, end = 24.dp)
            )
        }

        FloatingActionButton(
            onClick = onBackToMenu,
            modifier = Modifier.align(Alignment.BottomStart).padding(start = 24.dp, bottom = 48.dp),
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor   = MaterialTheme.colorScheme.onSecondaryContainer
        ) {
            Icon(Icons.Default.QrCodeScanner, contentDescription = "Vissza")
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope", "DefaultLocale")
@Composable
fun MapContent(
    startNodeId: String,
    goalNodeId: String,
    path: List<String>,
    isDarkMode: Boolean,
    currentLanguage: AppLanguage,
    onCheckpointRepath: (String) -> Unit = {},
    onTurnDirectionChanged: (TurnDirection) -> Unit = {}
) {
    val figmaWidth = 1080f; val figmaHeight = 1920f

    val infiniteTransition = rememberInfiniteTransition(label = "route")
    val dashPhase by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 60f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing), RepeatMode.Restart),
        label = "dashPhase"
    )
    val arrowScale by animateFloatAsState(
        targetValue = if (path.isNotEmpty()) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "arrowScale"
    )

    val primaryColor   = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor  = MaterialTheme.colorScheme.tertiary
    val coroutineScope = rememberCoroutineScope()

    val startNode = remember(startNodeId) { MapData.nodes.first { it.id == startNodeId } }
    var currentVisibleFloor by remember(startNodeId) { mutableStateOf(startNode.floor) }

    val rawAzimuth = rememberDeviceAzimuth()
    val arrowAngle = (rawAzimuth - 279f + 360f) % 360f

    var currentX by remember(startNodeId) { mutableFloatStateOf(startNode.x) }
    var currentY by remember(startNodeId) { mutableFloatStateOf(startNode.y) }
    val animatedX by animateFloatAsState(currentX, tween(300, easing = LinearEasing), label = "animX")
    val animatedY by animateFloatAsState(currentY, tween(300, easing = LinearEasing), label = "animY")

    var targetPathIndex by remember(startNodeId, path) { mutableIntStateOf(if (path.size > 1) 1 else 0) }

    // Checkpoint snap figyelése
    val lastCheckpoint by CheckpointManager.lastCheckpoint.collectAsState()
    var lastSnapNodeId by remember { mutableStateOf<String?>(null) }


    LaunchedEffect(lastCheckpoint) {
        val checkpoint = lastCheckpoint ?: return@LaunchedEffect
        if (checkpoint.nodeId == lastSnapNodeId) return@LaunchedEffect

        val checkpointNode = MapData.nodes.firstOrNull { it.id == checkpoint.nodeId } ?: return@LaunchedEffect

        // Snap: pozíció átugrik a checkpoint node-ra
        currentX = checkpointNode.x
        currentY = checkpointNode.y
        lastSnapNodeId = checkpoint.nodeId

        // Ha a checkpoint szerepel az útvonalon, csak az indexet frissítjük
        // Ha nem szerepel, újraszámítjuk az útvonalat a checkpoint node-tól a célig
        val nodeIndexInPath = path.indexOf(checkpoint.nodeId)
        if (nodeIndexInPath >= 0 && nodeIndexInPath < path.size - 1) {
            targetPathIndex = nodeIndexInPath + 1
        } else {
            // Újraszámítás jelzése a MapScreen-nek - ez triggereli az A* újrafuttatását
            // a checkpoint node-tól, így az útvonal mindig a folyosón marad
            targetPathIndex = 0
            onCheckpointRepath(checkpoint.nodeId)
        }

        CheckpointManager.resetLastCheckpoint()
        lastSnapNodeId = null
    }

    // Az aktuális szögeltérés a helyes iránytól - folyamatosan frissül arrowAngle változásakor
    var currentAngleDiff by remember { mutableFloatStateOf(0f) }

    // Animált szín a glow körnek - fade átmenettel vált zöld/sárga/piros között
    val targetGlowColor = when {
        currentAngleDiff <= 20f -> Color(0xFF4CAF50)
        currentAngleDiff <= 45f -> Color(0xFFFFC107)
        else                    -> Color(0xFFF44336)
    }
    val animatedGlowColor by animateColorAsState(
        targetValue = targetGlowColor,
        animationSpec = tween(durationMillis = 600),
        label = "glowColor"
    )

    // Folyamatosan számoljuk az angleDiff-et, ne csak lépéskor
    if (path.isNotEmpty() && targetPathIndex < path.size) {
        val target = MapData.nodes.firstOrNull { it.id == path[targetPathIndex] }
        if (target != null) {
            val dx = target.x - currentX; val dy = target.y - currentY
            var pathAngle = Math.toDegrees(kotlin.math.atan2(dy.toDouble(), dx.toDouble())).toFloat() + 90f
            pathAngle = (pathAngle + 360f) % 360f
            var diff = Math.abs(arrowAngle - pathAngle)
            if (diff > 180f) diff = 360f - diff
            currentAngleDiff = diff
        }
    }

    // Fordulási irány számítása a következő két node alapján
    if (path.size > targetPathIndex + 1) {
        val curr = MapData.nodes.firstOrNull { it.id == path[targetPathIndex] }
        val next = MapData.nodes.firstOrNull { it.id == path[targetPathIndex + 1] }
        val prev = if (targetPathIndex > 0)
            MapData.nodes.firstOrNull { it.id == path[targetPathIndex - 1] } else null

        if (curr != null && next != null && prev != null) {
            val inAngle  = Math.toDegrees(kotlin.math.atan2((curr.y - prev.y).toDouble(), (curr.x - prev.x).toDouble())).toFloat()
            val outAngle = Math.toDegrees(kotlin.math.atan2((next.y - curr.y).toDouble(), (next.x - curr.x).toDouble())).toFloat()
            var turn = outAngle - inAngle
            if (turn > 180f) turn -= 360f
            if (turn < -180f) turn += 360f

            val direction = when {
                turn < -60f  -> TurnDirection.LEFT
                turn < -20f  -> TurnDirection.SLIGHT_LEFT
                turn >  60f  -> TurnDirection.RIGHT
                turn >  20f  -> TurnDirection.SLIGHT_RIGHT
                else         -> TurnDirection.STRAIGHT
            }
            onTurnDirectionChanged(direction)
        }
    } else if (path.isNotEmpty()) {
        onTurnDirectionChanged(TurnDirection.STRAIGHT)
    }

    val performStep = {
        if (path.isNotEmpty() && targetPathIndex < path.size) {
            val target = MapData.nodes.first { it.id == path[targetPathIndex] }
            val dx = target.x - currentX; val dy = target.y - currentY
            var pathAngle = Math.toDegrees(kotlin.math.atan2(dy.toDouble(), dx.toDouble())).toFloat() + 90f
            pathAngle = (pathAngle + 360f) % 360f
            var angleDiff = Math.abs(arrowAngle - pathAngle)
            if (angleDiff > 180f) angleDiff = 360f - angleDiff
            if (angleDiff <= 45f) {
                val dist = kotlin.math.sqrt((dx * dx) + (dy * dy))
                if (dist <= 25f) {
                    currentX = target.x; currentY = target.y
                    if (targetPathIndex < path.size - 1) targetPathIndex++
                } else {
                    val r = 25f / dist; currentX += dx * r; currentY += dy * r
                }
            }
        }
    }
    rememberStepDetector(rawAzimuth) { performStep() }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = constraints.maxWidth.toFloat()
        val screenHeight = constraints.maxHeight.toFloat()
        val screenCenterX = screenWidth / 2f; val screenCenterY = screenHeight / 2f
        val density = LocalDensity.current

        val imageAspect = figmaWidth / figmaHeight; val screenAspect = screenWidth / screenHeight
        val scale: Float; val offsetX: Float; val offsetY: Float
        if (screenAspect > imageAspect) {
            scale = screenHeight / figmaHeight; offsetX = (screenWidth - figmaWidth * scale) / 2f; offsetY = 0f
        } else {
            scale = screenWidth / figmaWidth; offsetX = 0f; offsetY = (screenHeight - figmaHeight * scale) / 2f
        }

        val startScreenX = offsetX + animatedX * scale
        val startScreenY = offsetY + animatedY * scale

        val zoomScale = remember { Animatable(2f) }; val mapRotation = remember { Animatable(0f) }
        val panX = remember { Animatable(0f) }; val panY = remember { Animatable(0f) }
        var isTrackingMode by remember { mutableStateOf(true) }
        var isInitialized by remember { mutableStateOf(false) }; var lastStartNode by remember { mutableStateOf(startNodeId) }

        fun recenterParams(): Triple<Float, Float, Float> {
            val dx = startScreenX - screenCenterX; val dy = startScreenY - screenCenterY
            val rad = Math.toRadians((-arrowAngle).toDouble())
            val cos = kotlin.math.cos(rad).toFloat(); val sin = kotlin.math.sin(rad).toFloat()
            val sx = dx * 2f; val sy = dy * 2f; val yOff = screenHeight * 0.3f
            val tpx = -(sx * cos - sy * sin); val tpy = -(sx * sin + sy * cos) + yOff
            val curRot = mapRotation.value; val diff = (-arrowAngle - curRot) % 360f
            val nd = if (diff > 180f) diff - 360f else if (diff < -180f) diff + 360f else diff
            return Triple(tpx, tpy, curRot + nd)
        }

        val performRecenter = {
            isTrackingMode = false; currentVisibleFloor = startNode.floor
            val (tpx, tpy, rot) = recenterParams()
            coroutineScope.launch {
                val spec = tween<Float>(800, easing = FastOutSlowInEasing)
                launch { zoomScale.animateTo(2f, spec) }; launch { mapRotation.animateTo(rot, spec) }
                launch { panX.animateTo(tpx, spec) }; launch { panY.animateTo(tpy, spec) }
            }.invokeOnCompletion { isTrackingMode = true }
        }

        if (!isInitialized || startNodeId != lastStartNode) {
            LaunchedEffect(startNodeId) {
                val (tpx, tpy, rot) = recenterParams()
                zoomScale.snapTo(2f); mapRotation.snapTo(rot)
                panX.snapTo(tpx); panY.snapTo(tpy)
                isInitialized = true; lastStartNode = startNodeId
            }
        }

        LaunchedEffect(animatedX, animatedY, arrowAngle, isTrackingMode, zoomScale.value) {
            if (isTrackingMode) {
                val dx = startScreenX - screenCenterX; val dy = startScreenY - screenCenterY
                val rad = Math.toRadians((-arrowAngle).toDouble())
                val cos = kotlin.math.cos(rad).toFloat(); val sin = kotlin.math.sin(rad).toFloat()
                val sx = dx * zoomScale.value; val sy = dy * zoomScale.value; val yOff = screenHeight * 0.3f
                val tpx = -(sx * cos - sy * sin); val tpy = -(sx * sin + sy * cos) + yOff
                val curRot = mapRotation.value; val diff = (-arrowAngle - curRot) % 360f
                val nd = if (diff > 180f) diff - 360f else if (diff < -180f) diff + 360f else diff
                panX.snapTo(tpx); panY.snapTo(tpy); mapRotation.snapTo(curRot + nd)
            }
        }

        Box(
            modifier = Modifier.fillMaxSize()
                .background(if (isDarkMode) Color(0xFF121212) else Color(0xFFFFFFFF))
                .pointerInput(Unit) {
                    detectTransformGestures { centroid, pan, zoom, rotation ->
                        isTrackingMode = false
                        coroutineScope.launch {
                            val oldScale = zoomScale.value; val newScale = (oldScale * zoom).coerceIn(1f, 5f); val ez = newScale / oldScale
                            val bx = screenCenterX + panX.value; val by = screenCenterY + panY.value
                            val dx = bx - centroid.x; val dy = by - centroid.y
                            val sx = dx * ez; val sy = dy * ez
                            val rad = Math.toRadians(rotation.toDouble())
                            val cos = kotlin.math.cos(rad).toFloat(); val sin = kotlin.math.sin(rad).toFloat()
                            val newBx = centroid.x + (sx * cos - sy * sin) + pan.x
                            val newBy = centroid.y + (sx * sin + sy * cos) + pan.y
                            launch { zoomScale.snapTo(newScale) }; launch { panX.snapTo(newBx - screenCenterX) }
                            launch { panY.snapTo(newBy - screenCenterY) }; launch { mapRotation.snapTo(mapRotation.value + rotation) }
                        }
                    }
                }
        ) {
            Box(
                modifier = Modifier.fillMaxSize().graphicsLayer(
                    scaleX = zoomScale.value, scaleY = zoomScale.value,
                    translationX = panX.value, translationY = panY.value, rotationZ = mapRotation.value
                )
            ) {
                val mapRes = when (currentVisibleFloor) {
                    1 -> when (currentLanguage) {
                        AppLanguage.HU -> if (isDarkMode) R.drawable.map_vector_lvl1_dark else R.drawable.map_vector_lvl1
                        AppLanguage.EN -> if (isDarkMode) R.drawable.map_vector_lvl1_en_dark else R.drawable.map_vector_lvl1_en
                    }
                    2 -> when (currentLanguage) {
                        AppLanguage.HU -> if (isDarkMode) R.drawable.map_vector_lvl2_dark else R.drawable.map_vector_lvl2
                        AppLanguage.EN -> if (isDarkMode) R.drawable.map_vector_lvl2_en_dark else R.drawable.map_vector_lvl2_en
                    }
                    else -> R.drawable.map_vector_lvl1
                }
                Image(painter = painterResource(mapRes), contentDescription = null,
                    contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize())

                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (path.isNotEmpty() && targetPathIndex < path.size) {
                        val targetNode = MapData.nodes.first { it.id == path[targetPathIndex] }
                        val prevNode   = MapData.nodes.first { it.id == if (targetPathIndex > 0) path[targetPathIndex - 1] else path[0] }
                        if (prevNode.floor == currentVisibleFloor && targetNode.floor == currentVisibleFloor) {
                            drawRouteLine(Offset(offsetX + animatedX * scale, offsetY + animatedY * scale),
                                Offset(offsetX + targetNode.x * scale, offsetY + targetNode.y * scale), primaryColor, dashPhase)
                        }
                        for (i in targetPathIndex until path.size - 1) {
                            val from = MapData.nodes.first { it.id == path[i] }
                            val to   = MapData.nodes.first { it.id == path[i + 1] }
                            if (from.floor == currentVisibleFloor && to.floor == currentVisibleFloor) {
                                drawRouteLine(Offset(offsetX + from.x * scale, offsetY + from.y * scale),
                                    Offset(offsetX + to.x * scale, offsetY + to.y * scale), primaryColor, dashPhase)
                            }
                        }
                    }

                    // Checkpoint snap vizuális visszajelzés - pulzáló kör
                    // Irány visszajelző: glowing kör a nyíl körül (animált színátmenettel)
                    if (path.isNotEmpty() && startNode.floor == currentVisibleFloor) {
                        val cx = offsetX + animatedX * scale
                        val cy = offsetY + animatedY * scale
                        val arrowRadiusPx = with(density) { 20.dp.toPx() }
                        val glowRadius = arrowRadiusPx * 1.4f

                        drawCircle(color = animatedGlowColor.copy(alpha = 0.10f), radius = glowRadius * 1.3f, center = Offset(cx, cy))
                        drawCircle(color = animatedGlowColor.copy(alpha = 0.22f), radius = glowRadius, center = Offset(cx, cy))
                        drawCircle(
                            color = animatedGlowColor.copy(alpha = 0.50f),
                            radius = arrowRadiusPx * 1.25f,
                            center = Offset(cx, cy),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = with(density) { 2.5.dp.toPx() })
                        )
                    }
                }

                // Lépcső ikonok
                MapData.nodes.filter { it.id in STAIR_NODE_IDS && it.floor == currentVisibleFloor }.forEach { node ->
                    NodeIcon(node, offsetX, offsetY, scale, Icons.Default.Stairs,
                        tint    = if (isDarkMode) Color(0xFFAAAAAA) else Color(0xFF757575),
                        bgColor = if (isDarkMode) Color(0xFF2A2A2A) else Color(0xFFF0F0F0))
                }

                // Lift ikonok
                MapData.nodes.filter { it.id in ELEVATOR_NODE_IDS && it.floor == currentVisibleFloor }.forEach { node ->
                    NodeIcon(node, offsetX, offsetY, scale, Icons.Default.Elevator,
                        tint    = MaterialTheme.colorScheme.primary,
                        bgColor = MaterialTheme.colorScheme.primaryContainer)
                }

                // Cél pin
                val goalNode = MapData.nodes.first { it.id == goalNodeId }
                if (goalNode.floor == currentVisibleFloor) {
                    PinMarker(goalNode, offsetX, offsetY, scale, tertiaryColor)
                }

                // Navigációs nyíl + irány visszajelző ív
                if (startNode.floor == currentVisibleFloor) {
                    val sizeDp = 40.dp
                    val iconDp = 34.dp
                    val sizePx = with(density) { sizeDp.toPx() }

                    // Navigációs nyíl
                    Box(
                        modifier = Modifier
                            .offset { IntOffset((startScreenX - sizePx / 2f).roundToInt(), (startScreenY - sizePx / 2f).roundToInt()) }
                            .size(sizeDp)
                            .graphicsLayer(rotationZ = arrowAngle, scaleX = arrowScale, scaleY = arrowScale)
                            .shadow(6.dp, CircleShape, clip = false)
                            .clip(CircleShape)
                            .background(secondaryColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Navigation, contentDescription = null, tint = Color.White, modifier = Modifier.size(iconDp))
                    }
                }
            }
        }

        Column(
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledTonalIconButton(onClick = { currentVisibleFloor = 2 },
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = if (currentVisibleFloor == 2) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
            ) { Text("2", fontWeight = if (currentVisibleFloor == 2) FontWeight.Bold else FontWeight.Normal) }
            FilledTonalIconButton(onClick = { currentVisibleFloor = 1 },
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = if (currentVisibleFloor == 1) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
            ) { Text("1", fontWeight = if (currentVisibleFloor == 1) FontWeight.Bold else FontWeight.Normal) }
        }

        FloatingActionButton(
            onClick = { performRecenter() },
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 24.dp, bottom = 48.dp),
            containerColor = if (isTrackingMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            contentColor   = if (isTrackingMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Default.MyLocation, contentDescription = Translations.mapRecenter(currentLanguage))
        }
    }
}

// ── Segéd Composable-ök ──────────────────────────────────────────────────────

@Composable
private fun TurnPanel(
    direction: TurnDirection,
    modifier: Modifier = Modifier
) {
    val icon = when (direction) {
        TurnDirection.STRAIGHT     -> Icons.Default.Straight
        TurnDirection.SLIGHT_LEFT  -> Icons.Default.TurnSlightLeft
        TurnDirection.LEFT         -> Icons.Default.TurnLeft
        TurnDirection.SLIGHT_RIGHT -> Icons.Default.TurnSlightRight
        TurnDirection.RIGHT        -> Icons.Default.TurnRight
    }

    val animatedColor by animateColorAsState(
        targetValue = when (direction) {
            TurnDirection.STRAIGHT    -> Color(0xFF4CAF50)
            TurnDirection.SLIGHT_LEFT, TurnDirection.SLIGHT_RIGHT -> Color(0xFFFFC107)
            TurnDirection.LEFT, TurnDirection.RIGHT -> Color(0xFF2196F3)
        },
        animationSpec = tween(400),
        label = "turnColor"
    )

    Surface(
        modifier = modifier.size(64.dp),
        shape = RoundedCornerShape(20.dp),
        color = animatedColor.copy(alpha = 0.15f),
        tonalElevation = 4.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = animatedColor,
                modifier = Modifier.size(38.dp)
            )
        }
    }
}

@Composable
private fun NodeIcon(
    node: Node, offsetX: Float, offsetY: Float, scale: Float,
    icon: ImageVector, tint: Color, bgColor: Color
) {
    val density = LocalDensity.current
    val sizeDp = 28.dp; val sizePx = with(density) { sizeDp.toPx() }
    val sx = offsetX + node.x * scale; val sy = offsetY + node.y * scale
    Box(
        modifier = Modifier
            .offset { IntOffset((sx - sizePx / 2f).roundToInt(), (sy - sizePx / 2f).roundToInt()) }
            .size(sizeDp)
            .shadow(2.dp, CircleShape, clip = false)
            .clip(CircleShape)
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun PinMarker(node: Node, offsetX: Float, offsetY: Float, scale: Float, color: Color) {
    val density = LocalDensity.current
    val iconSize = 36.dp; val iconSizePx = with(density) { iconSize.toPx() }
    val sx = offsetX + node.x * scale; val sy = offsetY + node.y * scale
    Box(
        modifier = Modifier
            .offset { IntOffset((sx - iconSizePx / 2f).roundToInt(), (sy - iconSizePx).roundToInt()) }
            .size(iconSize)
    ) {
        Icon(Icons.Default.LocationOn, contentDescription = null, tint = color, modifier = Modifier.fillMaxSize())
    }
}

private fun DrawScope.drawRouteLine(start: Offset, end: Offset, primaryColor: Color, dashPhase: Float) {
    drawLine(color = primaryColor.copy(alpha = 0.18f), start = start, end = end, strokeWidth = 36f, cap = StrokeCap.Round)
    drawLine(color = primaryColor.copy(alpha = 0.55f), start = start, end = end, strokeWidth = 14f, cap = StrokeCap.Round)
    drawLine(color = Color.White.copy(alpha = 0.75f), start = start, end = end, strokeWidth = 14f, cap = StrokeCap.Round,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 40f), phase = -dashPhase))
}

// ── DestinationCard ──────────────────────────────────────────────────────────

fun formatTime(seconds: Int): String {
    return if (seconds < 60) "${seconds}s"
    else "${seconds / 60}p ${seconds % 60}s"
}

@Composable
fun DestinationCard(
    destinations: List<Pair<String, String>>,
    selected: Pair<String, String>,
    onSelected: (Pair<String, String>) -> Unit,
    onToggleDarkMode: () -> Unit,
    isDarkMode: Boolean,
    currentLanguage: AppLanguage,
    isAccessibleMode: Boolean,
    onAccessibleModeChange: (Boolean) -> Unit,
    estimatedSeconds: Int = 0,
    modifier: Modifier = Modifier
) {
    var expanded    by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val filteredDestinations = remember(searchQuery, destinations) {
        destinations.filter { it.second.contains(searchQuery, ignoreCase = true) }
    }

    Surface(
        modifier = modifier.fillMaxWidth(0.92f)
            .animateContentSize(spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)),
        shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 4.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (!expanded) {
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { expanded = true }
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(Translations.mapDestination(currentLanguage), style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(2.dp))
                        Text(selected.second, style = MaterialTheme.typography.titleMedium, fontWeight = SemiBold)
                        if (estimatedSeconds > 0) {
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.DirectionsWalk, null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(13.dp))
                                Spacer(Modifier.width(3.dp))
                                Text(
                                    text = "~${formatTime(estimatedSeconds)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                            }
                        }
                    }
                    FilledIconButton(
                        onClick = { onAccessibleModeChange(!isAccessibleMode) },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = if (isAccessibleMode) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Default.Accessibility, Translations.mapAccessibleToggle(currentLanguage),
                            tint = if (isAccessibleMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                    FilledIconButton(
                        onClick = onToggleDarkMode,
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode, null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(20.dp))
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp)) {
                    OutlinedTextField(
                        value = searchQuery, onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        placeholder = { Text(Translations.mapSearchPlaceholder(currentLanguage)) },
                        leadingIcon = { IconButton(onClick = { expanded = false; searchQuery = "" }) { Icon(Icons.Default.ArrowBack, null) } },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Close, null) }
                            else Icon(Icons.Default.Search, null)
                        },
                        singleLine = true, shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant)
                    )
                    if (searchQuery.isEmpty()) {
                        LazyRow(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            destinations.find { it.first == "n7" }?.let { d -> item {
                                AssistChip(onClick = { onSelected(d); expanded = false }, label = { Text(d.second) },
                                    leadingIcon = { Icon(Icons.Default.LocalCafe, null, Modifier.size(AssistChipDefaults.IconSize)) }) } }
                            destinations.find { it.first == "n16" }?.let { d -> item {
                                AssistChip(onClick = { onSelected(d); expanded = false }, label = { Text(d.second) },
                                    leadingIcon = { Icon(Icons.Default.Wc, null, Modifier.size(AssistChipDefaults.IconSize)) }) } }
                            destinations.find { it.first == "n17" }?.let { d -> item {
                                AssistChip(onClick = { onSelected(d); expanded = false }, label = { Text(d.second) },
                                    leadingIcon = { Icon(Icons.Default.Wc, null, Modifier.size(AssistChipDefaults.IconSize)) }) } }
                        }
                    } else Spacer(Modifier.height(8.dp))

                    LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 280.dp).padding(horizontal = 8.dp)) {
                        if (filteredDestinations.isEmpty()) {
                            item {
                                Text(Translations.mapNoResults(currentLanguage, searchQuery),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                                    textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else {
                            items(filteredDestinations) { dest ->
                                val isSel = dest.first == selected.first
                                Row(
                                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                                        .clickable { onSelected(dest); expanded = false; searchQuery = "" }
                                        .background(if (isSel) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else Color.Transparent)
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.LocationOn, null,
                                        tint = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(16.dp))
                                    Text(dest.second, style = MaterialTheme.typography.bodyLarge,
                                        color = if (isSel) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = if (isSel) SemiBold else FontWeight.Normal)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Szenzorok ────────────────────────────────────────────────────────────────

@Composable
fun rememberDeviceAzimuth(): Float {
    val context = LocalContext.current
    val azimuth = remember { mutableFloatStateOf(0f) }
    DisposableEffect(Unit) {
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event?.sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
                    val rm = FloatArray(9); SensorManager.getRotationMatrixFromVector(rm, event.values)
                    val oa = FloatArray(3); SensorManager.getOrientation(rm, oa)
                    var az = Math.toDegrees(oa[0].toDouble()).toFloat()
                    if (az < 0) az += 360f
                    val old = azimuth.floatValue; var diff = az - old
                    if (diff > 180f) diff -= 360f; if (diff < -180f) diff += 360f
                    azimuth.floatValue = (old + diff * 0.1f + 360f) % 360f
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        sm.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
        onDispose { sm.unregisterListener(listener) }
    }
    return azimuth.floatValue
}

@Composable
fun rememberStepDetector(currentAzimuth: Float, onStepDetected: () -> Unit) {
    val context = LocalContext.current
    val currentOnStep by rememberUpdatedState(onStepDetected)
    DisposableEffect(Unit) {
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val stepSensor = sm.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        var lastAz = currentAzimuth; var lastTime = System.currentTimeMillis()
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event?.sensor?.type == Sensor.TYPE_STEP_DETECTOR) {
                    val now = System.currentTimeMillis()
                    if (now - lastTime > 500L) { lastAz = currentAzimuth; lastTime = now }
                    var diff = Math.abs(currentAzimuth - lastAz)
                    if (diff > 180f) diff = 360f - diff
                    if (diff < 15f) currentOnStep()
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        if (stepSensor != null) sm.registerListener(listener, stepSensor, SensorManager.SENSOR_DELAY_FASTEST)
        onDispose { sm.unregisterListener(listener) }
    }
}