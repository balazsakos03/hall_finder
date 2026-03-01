package com.example.hall_finder.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.hall_finder.R
import com.example.hall_finder.graph.AStar
import com.example.hall_finder.model.MapData
import kotlin.math.exp
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun MapScreen(startNodeId: String) {

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

    val path = pathState.value

    Box(modifier = Modifier.fillMaxSize()) {

        // 🗺 MAP + ROUTE LAYER
        MapContent(
            startNodeId = startNodeId,
            goalNodeId = selectedDestination.value.first,
            path = path
        )

        // 🎯 FLOATING DESTINATION CARD
        DestinationCard(
            destinations = destinations,
            selected = selectedDestination.value,
            onSelected = { selectedDestination.value = it },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 24.dp)
        )
    }
}

@Composable
fun MapContent(
    startNodeId: String,
    goalNodeId: String,
    path: List<String>
) {
    val figmaWidth = 1080f
    val figmaHeight = 1920f

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val constraints = this.constraints
        val screenWidth = constraints.maxWidth.toFloat()
        val screenHeight = constraints.maxHeight.toFloat()

        val imageAspect = figmaWidth / figmaHeight
        val screenAspect = screenWidth / screenHeight

        val scale: Float
        val offsetX: Float
        val offsetY: Float

        val primaryColor = MaterialTheme.colorScheme.primary
        val secondaryColor = MaterialTheme.colorScheme.secondary
        val tertiaryColor = MaterialTheme.colorScheme.tertiary
        val surfaceColor = MaterialTheme.colorScheme.surface

        var zoomScale by remember {mutableStateOf(1f) }
        var panX by remember { mutableStateOf(0f) }
        var panY by remember { mutableStateOf(0f) }

        if (screenAspect > imageAspect) {
            scale = screenHeight / figmaHeight
            val imageWidth = figmaWidth * scale
            offsetX = (screenWidth - imageWidth) / 2f
            offsetY = 0f
        } else {
            scale = screenWidth / figmaWidth
            val imageHeight = figmaHeight * scale
            offsetX = 0f
            offsetY = (screenHeight - imageHeight) / 2f
        }

        Box(modifier = Modifier.fillMaxSize().pointerInput(Unit){
            detectTransformGestures { _, pan, zoom, _ ->
                val newScale = (zoomScale * zoom).coerceIn(1f, 4f)
                zoomScale = newScale

                val maxX = (constraints.maxWidth * (zoomScale - 1)) / 2
                val maxY = (constraints.maxHeight * (zoomScale - 1)) / 2

                panX = (panX + pan.x * 0.8f).coerceIn(-maxX, maxX)
                panY = (panY + pan.y * 0.8f).coerceIn(-maxY, maxY)
            }
        }.graphicsLayer(
            scaleX = zoomScale,
            scaleY = zoomScale,
            translationX = panX,
            translationY = panY
        )
        ){
            Image(
                painter = painterResource(id = R.drawable.map_vector),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )

            Canvas(modifier = Modifier.fillMaxSize()) {

                //utvonal rajzolas
                if (path.size > 1) {
                    for (i in 0 until path.size - 1) {

                        val fromNode = MapData.nodes.first { it.id == path[i] }
                        val toNode = MapData.nodes.first { it.id == path[i + 1] }

                        drawLine(
                            color = primaryColor.copy(alpha = 0.25f),
                            start = Offset(
                                offsetX + fromNode.x * scale,
                                offsetY + fromNode.y * scale
                            ),
                            end = Offset(
                                offsetX + toNode.x * scale,
                                offsetY + toNode.y * scale
                            ),
                            strokeWidth = 32f,
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = primaryColor,
                            start = Offset(
                                offsetX + fromNode.x * scale,
                                offsetY + fromNode.y * scale
                            ),
                            end = Offset(
                                offsetX + toNode.x * scale,
                                offsetY + toNode.y * scale
                            ),
                            strokeWidth = 18f,
                            cap = StrokeCap.Round
                        )
                    }
                }

                //kezdo pozicio
                val startNode = MapData.nodes.first { it.id == startNodeId }
                drawCircle(
                    color = surfaceColor,
                    radius = 26f,
                    center = Offset(
                        offsetX + startNode.x * scale,
                        offsetY + startNode.y * scale
                    )
                )
                drawCircle(
                    color = secondaryColor,
                    radius = 16f,
                    center = Offset(
                        offsetX + startNode.x * scale,
                        offsetY + startNode.y * scale
                    )
                )

                //cel
                val goalNode = MapData.nodes.first { it.id == goalNodeId }
                drawCircle(
                    color = surfaceColor,
                    radius = 26f,
                    center = Offset(
                        offsetX + goalNode.x * scale,
                        offsetY + goalNode.y * scale
                    )
                )
                drawCircle(
                    color = tertiaryColor,
                    radius = 16f,
                    center = Offset(
                        offsetX + goalNode.x * scale,
                        offsetY + goalNode.y * scale
                    )
                )
            }
        }
    }
}

@Composable
fun DestinationCard(
    destinations: List<Pair<String, String>>,
    selected: Pair<String, String>,
    onSelected: (Pair<String, String>) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = modifier.fillMaxWidth(0.9f),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {

            Text(
                text = selected.second,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true }
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                destinations.forEach { destination ->
                    DropdownMenuItem(
                        text = { Text(destination.second) },
                        onClick = {
                            onSelected(destination)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}