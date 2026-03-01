package com.example.hall_finder.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.hall_finder.R
import com.example.hall_finder.graph.AStar
import com.example.hall_finder.model.MapData

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun MapScreen() {

    val pathState = remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(Unit) {
        val aStar = AStar(MapData.graph, MapData.nodes)
        pathState.value = aStar.findPath("n1", "n15")
    }

    val path = pathState.value

    val figmaWidth = 1080f
    val figmaHeight = 1920f

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
                //nodeok kirajzolasa
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

                //utvonal kirajzolasa
                if(path.size > 1){
                    for(i in 0 until path.size -1){
                        val fromNode = MapData.nodes.first{it.id == path[i]}
                        val toNode = MapData.nodes.first{it.id == path[i+1]}

                        drawLine(
                            color = androidx.compose.ui.graphics.Color.Blue,
                            start = androidx.compose.ui.geometry.Offset(
                                offsetX + fromNode.x * scale,
                                offsetY + fromNode.y * scale
                            ),
                            end = androidx.compose.ui.geometry.Offset(
                                offsetX + toNode.x * scale,
                                offsetY + toNode.y *scale
                            ),
                            strokeWidth = 12f
                        )
                    }
                }
            }
        }
    }
}