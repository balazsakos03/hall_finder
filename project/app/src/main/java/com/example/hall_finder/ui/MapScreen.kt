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

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun MapScreen(
    startNodeId: String,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    currentLanguage: AppLanguage
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

        // map+route layer
        MapContent(
            startNodeId = startNodeId,
            goalNodeId = selectedDestinationId.value,
            path = pathState.value,
            isDarkMode = isDarkMode,
            currentLanguage = currentLanguage
        )

        // uticel kivalasztasa
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
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
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
                val mapImageRes = when (currentLanguage) {
                    AppLanguage.HU -> {
                        if (isDarkMode) R.drawable.map_vector_dark
                        else R.drawable.map_vector
                    }
                    AppLanguage.EN -> {
                        if (isDarkMode) R.drawable.map_vector_dark_en
                        else R.drawable.map_vector_en
                    }
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
                contentDescription = Translations.mapRecenter(currentLanguage) // <-- Itt a valtoztatas
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
    currentLanguage: AppLanguage,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    //szurt lista a kereses alapjan
    val filteredDestinations = remember(searchQuery, destinations) {
        destinations.filter { it.second.contains(searchQuery, ignoreCase = true) }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth(0.92f)
            //kartya meret animalasa
            .animateContentSize(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)),
        shape     = RoundedCornerShape(28.dp),
        color     = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            if (!expanded) {
                //osszecsukott allapot
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = true } //erre nyilik le a kereso
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
                //kinyitott allapott: keresosav + gyorsgombok + gorgetheto lista
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 8.dp)
                ) {
                    // 1. Keresomezo
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

                    // 2. ÚJ: GYORSGOMBOK (Quick Actions)
                    // Csak akkor mutatjuk, ha nem gépelt még be semmit a felhasználó
                    if (searchQuery.isEmpty()) {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Büfé gyorsgomb (n7)
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

                            // Férfi mosdó (n16)
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

                            // Női mosdó (n17)
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
                                                Icons.Default.Wc, // Lehetne külön női ikon is, de a WC ikon egyértelmű
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

                    // 3. Talalatok listaja
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp) //ne takarja ki az egesz kepernyot de lehessen gorgetni
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
                                            onSelected(dest) //kivalasztas
                                            expanded = false //bezaras
                                            searchQuery = "" //kereses torlese
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