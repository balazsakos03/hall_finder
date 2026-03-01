package com.example.hall_finder.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

    LaunchedEffect(startNodeId, selectedDestination.value) {
        val aStar = AStar(MapData.graph, MapData.nodes)
        pathState.value = aStar.findPath(
            startNodeId,
            selectedDestination.value.first
        )
    }

    val path = pathState.value

    val figmaWidth = 1080f
    val figmaHeight = 1920f

    Column(modifier = Modifier.fillMaxSize()){
        //cel kivalaszto
        DestinationDropdown(
            destinations = destinations,
            selected = selectedDestination.value,
            onSelected = {selectedDestination.value = it}
        )

        BoxWithConstraints(
                modifier = Modifier.fillMaxSize()
                ) {

            val screenWidth = constraints.maxWidth.toFloat()
            val screenHeight = constraints.maxHeight.toFloat()

            val imageAspect = figmaWidth / figmaHeight
            val screenAspect = screenWidth / screenHeight

            val scale: Float
            val imageWidth: Float
            val imageHeight: Float
            val offsetX: Float
            val offsetY: Float

            if (screenAspect > imageAspect) {
                // Screen wider than image
                scale = screenHeight / figmaHeight
                imageHeight = screenHeight
                imageWidth = figmaWidth * scale
                offsetX = (screenWidth - imageWidth) / 2f
                offsetY = 0f
            } else {
                // Screen taller than image
                scale = screenWidth / figmaWidth
                imageWidth = screenWidth
                imageHeight = figmaHeight * scale
                offsetX = 0f
                offsetY = (screenHeight - imageHeight) / 2f
            }

            Box(modifier = Modifier.fillMaxSize()) {

                Image(
                    painter = painterResource(id = R.drawable.map_vector),
                    contentDescription = "Map",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )

                Canvas(modifier = Modifier.fillMaxSize()) {
                    //nodeok kirajzolasa csak debug mode-ban
                    val debugMode = false
                    if(debugMode){
                        MapData.nodes.forEach { node ->
                        drawCircle(
                            color = androidx.compose.ui.graphics.Color.Red,
                            radius = 15f,
                            center = androidx.compose.ui.geometry.Offset(
                                offsetX + node.x * scale,
                                offsetY + node.y * scale
                            )
                        )
                        }
                    }

                    //utvonal kirajzolasa
                    if(path.size > 1){
                        for(i in 0 until path.size - 1){
                            val fromNode = MapData.nodes.first{it.id == path[i]}
                            val toNode = MapData.nodes.first{it.id == path[i+1]}

                            drawLine(
                                color = Color(0xFF2962FF),
                                start = Offset(
                                    offsetX + fromNode.x * scale,
                                    offsetY + fromNode.y * scale
                                ),
                                end = Offset(
                                    offsetX + toNode.x * scale,
                                    offsetY + toNode.y *scale
                                ),
                                strokeWidth = 18f,
                                cap = StrokeCap.Round
                            )
                        }
                    }

                    //start helyzet
                    val startNode = MapData.nodes.first{ it.id == startNodeId}
                    drawCircle(
                        color = Color.White,
                        radius = 28f,
                        center = Offset(
                            offsetX + startNode.x * scale,
                            offsetY + startNode.y * scale
                        )
                    )
                    drawCircle(
                        color = Color(0xFF00C853),
                        radius = 18f,
                        center = Offset(
                            offsetX + startNode.x * scale,
                            offsetY + startNode.y * scale
                        )
                    )

                    //cel jeloles
                    val goalNodeId = selectedDestination.value.first
                    val goalNode = MapData.nodes.first{ it.id == goalNodeId }
                    drawCircle(
                        color = Color.White,
                        radius = 28f,
                        center = Offset(
                            offsetX + goalNode.x * scale,
                            offsetY + goalNode.y *scale
                        )
                    )
                    drawCircle(
                        color = Color(0xFFAA00FF),
                        radius = 18f,
                        center = Offset(
                            offsetX + goalNode.x * scale,
                            offsetY + goalNode.y *scale
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun DestinationDropdown(
    destinations: List<Pair<String, String>>,
    selected: Pair<String, String>,
    onSelected: (Pair<String, String>) -> Unit
){
    var expanded by remember {mutableStateOf(false)}
    Box(modifier = Modifier.fillMaxWidth().padding(16.dp)){
        ElevatedCard(modifier = Modifier.fillMaxWidth(), onClick = {expanded=true}) {
            Text(
                text = "Cél: ${selected.second}",
                modifier = Modifier.padding(16.dp)
            )
        }

        DropdownMenu(expanded = expanded, onDismissRequest = {expanded = false}) {
            destinations.forEach {
                destination ->
                DropdownMenuItem(
                    text = {Text(destination.second)},
                    onClick = {
                        onSelected(destination)
                        expanded = false
                    }
                )
            }
        }
    }
}