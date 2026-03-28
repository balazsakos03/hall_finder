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
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.atan2
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.example.hall_finder.model.AppLanguage
import com.example.hall_finder.model.Translations
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Wc
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Slider
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberUpdatedState

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun MapScreen(
    startNodeId: String,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    currentLanguage: AppLanguage,
    onBackToMenu: () -> Unit
) {
    val destinations = remember(currentLanguage) {
        Translations.getDestinations(currentLanguage)
    }

    val selectedDestinationId = remember { mutableStateOf(destinations.first().first) }

    val currentSelectedPair = destinations.firstOrNull { it.first == selectedDestinationId.value }
        ?: destinations.first()

    val pathState = remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(startNodeId, selectedDestinationId.value) {
        val aStar = AStar(MapData.graph, MapData.nodes)
        pathState.value = aStar.findPath(
            startNodeId,
            selectedDestinationId.value
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MapContent(
            startNodeId = startNodeId,
            goalNodeId = selectedDestinationId.value,
            path = pathState.value,
            isDarkMode = isDarkMode,
            currentLanguage = currentLanguage
        )

        DestinationCard(
            destinations = destinations,
            selected = currentSelectedPair,
            onSelected = { selectedDestinationId.value = it.first },
            onToggleDarkMode = onToggleDarkMode,
            isDarkMode = isDarkMode,
            currentLanguage = currentLanguage,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 52.dp)
        )

        FloatingActionButton(
            onClick = onBackToMenu,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 24.dp, bottom = 48.dp),
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ) {
            Icon(
                imageVector = Icons.Default.QrCodeScanner,
                contentDescription = "Újraolvasás / Vissza"
            )
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
    currentLanguage: AppLanguage
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

    val coroutineScope = rememberCoroutineScope()

    val startNode = remember(startNodeId) { MapData.nodes.first { it.id == startNodeId } }
    var currentVisibleFloor by remember(startNodeId) { mutableStateOf(startNode.floor) }

    val mapNorthOffset = 279f
    val rawAzimuth = rememberDeviceAzimuth()
    val arrowAngle = (rawAzimuth - mapNorthOffset + 360f) % 360f

    var stepCount by remember { mutableIntStateOf(0) }

    var currentX by remember(startNodeId) { mutableFloatStateOf(startNode.x) }
    var currentY by remember(startNodeId) { mutableFloatStateOf(startNode.y) }

    val animatedX by animateFloatAsState(
        targetValue = currentX,
        animationSpec = tween(durationMillis = 300, easing = LinearEasing), label = "animX"
    )
    val animatedY by animateFloatAsState(
        targetValue = currentY,
        animationSpec = tween(durationMillis = 300, easing = LinearEasing), label = "animY"
    )

    var targetPathIndex by remember(startNodeId, path) { mutableIntStateOf(if (path.size > 1) 1 else 0) }
    val stepSizePixels = 25f
    val directionThreshold = 45f

    val performStep = {
        if (path.isNotEmpty() && targetPathIndex < path.size) {
            val targetNode = MapData.nodes.first { it.id == path[targetPathIndex] }
            val dx = targetNode.x - currentX
            val dy = targetNode.y - currentY

            var pathAngle = Math.toDegrees(kotlin.math.atan2(dy.toDouble(), dx.toDouble())).toFloat() + 90f
            pathAngle = (pathAngle + 360f) % 360f

            var angleDiff = Math.abs(arrowAngle - pathAngle)
            if (angleDiff > 180f) {
                angleDiff = 360f - angleDiff
            }

            if (angleDiff <= directionThreshold) {
                stepCount++
                val distanceToTarget = kotlin.math.sqrt((dx * dx) + (dy * dy))

                if (distanceToTarget <= stepSizePixels) {
                    currentX = targetNode.x
                    currentY = targetNode.y
                    if (targetPathIndex < path.size - 1) {
                        targetPathIndex++
                    }
                } else {
                    val ratio = stepSizePixels / distanceToTarget
                    currentX += dx * ratio
                    currentY += dy * ratio
                }
            }
        }
    }

    rememberStepDetector(
        currentAzimuth = rawAzimuth,
        onStepDetected = { performStep() }
    )

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

        val startScreenX = offsetX + animatedX * scale
        val startScreenY = offsetY + animatedY * scale

        val zoomScale = remember { Animatable(2f) }
        val mapRotation = remember { Animatable(0f) }
        val panX = remember { Animatable(0f) }
        val panY = remember { Animatable(0f) }

        var isTrackingMode by remember { mutableStateOf(true) }

        var isInitialized by remember { mutableStateOf(false) }
        var lastStartNode by remember { mutableStateOf(startNodeId) }

        val performRecenter = {
            isTrackingMode = false
            currentVisibleFloor = startNode.floor

            val dx = startScreenX - screenCenterX
            val dy = startScreenY - screenCenterY

            val rad = Math.toRadians((-arrowAngle).toDouble())
            val cos = kotlin.math.cos(rad).toFloat()
            val sin = kotlin.math.sin(rad).toFloat()

            val sx = dx * 2f
            val sy = dy * 2f
            val yOffset = screenHeight * 0.3f

            val targetPanX = -(sx * cos - sy * sin)
            val targetPanY = -(sx * sin + sy * cos) + yOffset

            val currentRot = mapRotation.value
            val targetRotRaw = -arrowAngle
            val diff = (targetRotRaw - currentRot) % 360f
            val normalizedDiff = if (diff > 180f) diff - 360f else if (diff < -180f) diff + 360f else diff
            val finalTargetRot = currentRot + normalizedDiff

            coroutineScope.launch {
                val animSpec = tween<Float>(durationMillis = 800, easing = FastOutSlowInEasing)
                launch { zoomScale.animateTo(2f, animationSpec = animSpec) }
                launch { mapRotation.animateTo(finalTargetRot, animationSpec = animSpec) }
                launch { panX.animateTo(targetPanX, animationSpec = animSpec) }
                launch { panY.animateTo(targetPanY, animationSpec = animSpec) }
            }.invokeOnCompletion {
                isTrackingMode = true
            }
        }

        if (!isInitialized || startNodeId != lastStartNode) {
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

        LaunchedEffect(animatedX, animatedY, arrowAngle, isTrackingMode, zoomScale.value) {
            if (isTrackingMode) {
                val dx = startScreenX - screenCenterX
                val dy = startScreenY - screenCenterY
                val rad = Math.toRadians((-arrowAngle).toDouble())
                val cos = kotlin.math.cos(rad).toFloat()
                val sin = kotlin.math.sin(rad).toFloat()

                val sx = dx * zoomScale.value
                val sy = dy * zoomScale.value
                val yOffset = screenHeight * 0.3f

                val targetPanX = -(sx * cos - sy * sin)
                val targetPanY = -(sx * sin + sy * cos) + yOffset

                val currentRot = mapRotation.value
                val diff = (-arrowAngle - currentRot) % 360f
                val normalizedDiff = if (diff > 180f) diff - 360f else if (diff < -180f) diff + 360f else diff
                val finalTargetRot = currentRot + normalizedDiff

                panX.snapTo(targetPanX)
                panY.snapTo(targetPanY)
                mapRotation.snapTo(finalTargetRot)
            }
        }

        val mapBgColor = if (isDarkMode) Color(0xFF121212) else Color(0xFFFFFFFF)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(mapBgColor)
                .pointerInput(Unit) {
                    detectTransformGestures { centroid, pan, zoom, rotation ->
                        isTrackingMode = false
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

                            launch { zoomScale.snapTo(newScale) }
                            launch { panX.snapTo(newBx - screenCenterX) }
                            launch { panY.snapTo(newBy - screenCenterY) }
                            launch { mapRotation.snapTo(mapRotation.value + rotation) }
                        }
                    }
                }
        ) {
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
                val mapImageRes = when (currentVisibleFloor) {
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

                Image(
                    painter = painterResource(id = mapImageRes),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )

                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (path.size > 1) {
                        for (i in 0 until path.size - 1) {
                            val from = MapData.nodes.first { it.id == path[i] }
                            val to = MapData.nodes.first { it.id == path[i + 1] }

                            if (from.floor == currentVisibleFloor && to.floor == currentVisibleFloor) {
                                val start = Offset(offsetX + from.x * scale, offsetY + from.y * scale)
                                val end   = Offset(offsetX + to.x   * scale, offsetY + to.y   * scale)

                                drawLine(color = primaryColor.copy(alpha = 0.18f), start = start, end = end, strokeWidth = 36f, cap = StrokeCap.Round)
                                drawLine(color = primaryColor.copy(alpha = 0.55f), start = start, end = end, strokeWidth = 14f, cap = StrokeCap.Round)
                                drawLine(
                                    color = Color.White.copy(alpha = 0.75f), start = start, end = end, strokeWidth = 14f, cap = StrokeCap.Round,
                                    pathEffect = PathEffect.dashPathEffect(intervals = floatArrayOf(20f, 40f), phase = -dashPhase)
                                )
                            }
                        }
                    }

                    val goalNode = MapData.nodes.first { it.id == goalNodeId }
                    if (goalNode.floor == currentVisibleFloor) {
                        drawPinMarker(
                            center = Offset(offsetX + goalNode.x * scale, offsetY + goalNode.y * scale),
                            color  = tertiaryColor, shadowColor = tertiaryColor.copy(alpha = 0.3f), scale  = scale
                        )
                    }

                    if (startNode.floor == currentVisibleFloor) {
                        val startCenter = Offset(offsetX + animatedX * scale, offsetY + animatedY * scale)

                        drawNavigationArrow(
                            center = startCenter, angleDeg = arrowAngle,
                            color = secondaryColor, shadowColor = secondaryColor.copy(alpha = 0.35f), arrowScale = arrowScale
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledTonalIconButton(
                onClick = { currentVisibleFloor = 2 },
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = if (currentVisibleFloor == 2) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text("2", fontWeight = if (currentVisibleFloor == 2) FontWeight.Bold else FontWeight.Normal)
            }
            FilledTonalIconButton(
                onClick = { currentVisibleFloor = 1 },
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = if (currentVisibleFloor == 1) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text("1", fontWeight = if (currentVisibleFloor == 1) FontWeight.Bold else FontWeight.Normal)
            }
        }

        FloatingActionButton(
            onClick = { performRecenter() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 48.dp),
            containerColor = if (isTrackingMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isTrackingMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
        ) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = Translations.mapRecenter(currentLanguage)
            )
        }
    }
}

private fun DrawScope.drawNavigationArrow(
    center: Offset,
    angleDeg: Float,
    color: Color,
    shadowColor: Color,
    arrowScale: Float
){
    val r = 32f * arrowScale
    rotate(degrees = angleDeg, pivot = center){
        drawCircle(color = shadowColor, radius = r * 1.9f, center = center)
        drawCircle(color = Color.White, radius = r * 1.35f, center = center)

        val path = Path().apply {
            moveTo(center.x, center.y - r * 1.3f)
            lineTo(center.x + r, center.y + r * 0.9f)
            lineTo(center.x, center.y + r * 0.3f)
            lineTo(center.x - r, center.y + r * 0.9f)
            close()
        }
        drawPath(path = path, color = color)

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
        moveTo(center.x - r * 0.55f, center.y - stemH - r * 0.3f)
        lineTo(center.x + r * 0.55f, center.y - stemH - r * 0.3f)
        lineTo(center.x,             center.y)
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
    currentLanguage: AppLanguage,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredDestinations = remember(searchQuery, destinations) {
        destinations.filter { it.second.contains(searchQuery, ignoreCase = true) }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth(0.92f)
            .animateContentSize(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)),
        shape     = RoundedCornerShape(28.dp),
        color     = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            if (!expanded) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = true }
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text  = Translations.mapDestination(currentLanguage),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text     = selected.second,
                            style    = MaterialTheme.typography.titleMedium,
                            fontWeight = SemiBold
                        )
                    }

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
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        placeholder = { Text(Translations.mapSearchPlaceholder(currentLanguage)) },
                        leadingIcon = {
                            IconButton(onClick = {
                                expanded = false
                                searchQuery = ""
                            }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Vissza")
                            }
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Törlés")
                                }
                            } else {
                                Icon(Icons.Default.Search, contentDescription = "Keresés")
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )

                    if (searchQuery.isEmpty()) {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val cafeDest = destinations.find { it.first == "n7" }
                            if (cafeDest != null) {
                                item {
                                    AssistChip(
                                        onClick = {
                                            onSelected(cafeDest)
                                            expanded = false
                                        },
                                        label = { Text(cafeDest.second) },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.LocalCafe,
                                                contentDescription = null,
                                                modifier = Modifier.size(AssistChipDefaults.IconSize)
                                            )
                                        }
                                    )
                                }
                            }

                            val mensWcDest = destinations.find { it.first == "n16" }
                            if (mensWcDest != null) {
                                item {
                                    AssistChip(
                                        onClick = {
                                            onSelected(mensWcDest)
                                            expanded = false
                                        },
                                        label = { Text(mensWcDest.second) },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Wc,
                                                contentDescription = null,
                                                modifier = Modifier.size(AssistChipDefaults.IconSize)
                                            )
                                        }
                                    )
                                }
                            }

                            val womensWcDest = destinations.find { it.first == "n17" }
                            if (womensWcDest != null) {
                                item {
                                    AssistChip(
                                        onClick = {
                                            onSelected(womensWcDest)
                                            expanded = false
                                        },
                                        label = { Text(womensWcDest.second) },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Wc,
                                                contentDescription = null,
                                                modifier = Modifier.size(AssistChipDefaults.IconSize)
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp)
                            .padding(horizontal = 8.dp)
                    ) {
                        if (filteredDestinations.isEmpty()) {
                            item {
                                Text(
                                    text = Translations.mapNoResults(currentLanguage, searchQuery),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 32.dp),
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            items(filteredDestinations) { dest ->
                                val isSelected = dest.first == selected.first
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable {
                                            onSelected(dest)
                                            expanded = false
                                            searchQuery = ""
                                        }
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                            else Color.Transparent
                                        )
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(
                                        text = dest.second,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = if (isSelected) SemiBold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun rememberDeviceAzimuth(): Float {
    val context = LocalContext.current
    val azimuth = remember { mutableFloatStateOf(0f) }

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event?.sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
                    val rotationMatrix = FloatArray(9)
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)

                    val orientationAngles = FloatArray(3)
                    SensorManager.getOrientation(rotationMatrix, orientationAngles)

                    var currentAzimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                    if (currentAzimuth < 0) currentAzimuth += 360f

                    val oldAzimuth = azimuth.floatValue
                    var diff = currentAzimuth - oldAzimuth

                    if (diff > 180f) diff -= 360f
                    if (diff < -180f) diff += 360f

                    azimuth.floatValue = (oldAzimuth + diff * 0.1f + 360f) % 360f
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, rotationVectorSensor, SensorManager.SENSOR_DELAY_UI)
        onDispose { sensorManager.unregisterListener(listener) }
    }
    return azimuth.floatValue
}

@Composable
fun rememberStepDetector(
    currentAzimuth: Float,
    onStepDetected: () -> Unit
) {
    val context = LocalContext.current
    val currentOnStepDetected by rememberUpdatedState(onStepDetected)

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

        var lastAzimuth = currentAzimuth
        var lastAzimuthTime = System.currentTimeMillis()

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event?.sensor?.type == Sensor.TYPE_STEP_DETECTOR) {
                    val currentTime = System.currentTimeMillis()

                    if (currentTime - lastAzimuthTime > 500L) {
                        lastAzimuth = currentAzimuth
                        lastAzimuthTime = currentTime
                    }

                    var azimuthDiff = Math.abs(currentAzimuth - lastAzimuth)
                    if (azimuthDiff > 180f) azimuthDiff = 360f - azimuthDiff

                    if (azimuthDiff < 15f) {
                        currentOnStepDetected()
                    }
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (stepSensor != null) {
            sensorManager.registerListener(listener, stepSensor, SensorManager.SENSOR_DELAY_FASTEST)
        }
        onDispose { sensorManager.unregisterListener(listener) }
    }
}