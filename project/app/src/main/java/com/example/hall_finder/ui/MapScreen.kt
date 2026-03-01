package com.example.hall_finder.ui

import android.R.attr.shadowColor
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.SemiBold
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

    //pulzalo utvonal animacio
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

    //megjelenes animacio
    val arrowScale by animateFloatAsState(
        targetValue = if(path.isNotEmpty()) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "arrowScale"
    )

    val primaryColor   = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary   // nyil szine
    val tertiaryColor  = MaterialTheme.colorScheme.tertiary    // cel pin szine
    val surfaceColor   = MaterialTheme.colorScheme.surface

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = constraints.maxWidth.toFloat()
        val screenHeight = constraints.maxHeight.toFloat()

        val imageAspect = figmaWidth / figmaHeight
        val screenAspect = screenWidth / screenHeight

        val scale: Float
        val offsetX: Float
        val offsetY: Float

        if(screenAspect > imageAspect){
            scale = screenHeight / figmaHeight
            offsetX = (screenWidth - figmaWidth * scale) / 2f
            offsetY = 0f
        }else{
            scale = screenWidth / figmaWidth
            offsetX = 0f
            offsetY = (screenHeight - figmaHeight * scale) / 2f
        }

        //kezdo node kepernyo koordinatai
        val startNode = MapData.nodes.first {it.id == startNodeId}
        val startScreenX = offsetX + startNode.x * scale
        val startScreenY = offsetY + startNode.y * scale

        //zoom/pan state
        var zoomScale by remember {mutableStateOf(2f) } //indulaskor 2x zoom
        //betolteskor a nyil keruljon kozepre
        var panX by remember { mutableStateOf((screenWidth  / 2f - startScreenX) * 2f) }
        var panY by remember { mutableStateOf((screenHeight / 2f - startScreenY) * 2f) }

        val arrowAngle = remember(path) {
            if(path.size >= 2){
                val from = MapData.nodes.first {it.id == path[0]}
                val to = MapData.nodes.first {it.id == path[1]}
                val dx = to.x - from.x
                val dy = to.y - from.y
                //canvas y tengelye lefele no -> atan2(dy,dx) adja a szoget
                //+90f mert a nyil alapbol lefele mutat
                Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat() + 90f
            }else 0f
        }

        Box(modifier = Modifier.fillMaxSize().pointerInput(Unit){
            detectTransformGestures { _, pan, zoom, _ ->
                val newScale = (zoomScale * zoom).coerceIn(1f, 5f)
                zoomScale = newScale

                val maxX = (screenWidth * (zoomScale - 1)) / 2f
                val maxY = (screenHeight * (zoomScale - 1)) / 2f

                panX = (panX + pan.x).coerceIn(-maxX, maxX)
                panY = (panY + pan.y).coerceIn(-maxY, maxY)
            }
        }.graphicsLayer(
            scaleX = zoomScale,
            scaleY = zoomScale,
            translationX = panX,
            translationY = panY
        )
        ){
            Image(
                painter = painterResource(
                    id = if (isDarkMode)
                        R.drawable.map_vector_dark
                    else
                        R.drawable.map_vector
                ),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )

            Canvas(modifier = Modifier.fillMaxSize()) {

                //utvonal rajzolas - pulzalo dash update
                if (path.size > 1) {
                    for (i in 0 until path.size - 1) {

                        val from = MapData.nodes.first { it.id == path[i] }
                        val to = MapData.nodes.first { it.id == path[i + 1] }

                        val start = Offset(offsetX + from.x * scale, offsetY + from.y * scale)
                        val end   = Offset(offsetX + to.x   * scale, offsetY + to.y   * scale)

                        //arnyek, glow
                        drawLine(
                            color = primaryColor.copy(alpha = 0.18f),
                            start = start,
                            end = end,
                            strokeWidth = 36f,
                            cap = StrokeCap.Round
                        )
                        //alap vonal
                        drawLine(
                            color = primaryColor.copy(alpha = 0.55f),
                            start = start,
                            end = end,
                            strokeWidth = 14f,
                            cap = StrokeCap.Round
                        )
                        //animalt dash
                        drawLine(
                            color = Color.White.copy(alpha = 0.75f),
                            start = start,
                            end = end,
                            strokeWidth = 14f,
                            cap = StrokeCap.Round,
                            pathEffect = PathEffect.dashPathEffect(
                                intervals = floatArrayOf(20f, 40f),
                                phase = -dashPhase
                            )
                        )
                    }
                }

                //cel pin marker
                val goalNode = MapData.nodes.first{it.id == goalNodeId}
                drawPinMarker(
                    center = Offset(offsetX + goalNode.x * scale, offsetY + goalNode.y * scale),
                    color  = tertiaryColor,
                    shadowColor = tertiaryColor.copy(alpha = 0.3f),
                    scale  = scale
                )


                //jelenlegi pozicio
                val startCenter = Offset(
                    offsetX + startNode.x * scale,
                    offsetY + startNode.y * scale
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