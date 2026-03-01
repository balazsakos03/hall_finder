package com.example.hall_finder

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.hall_finder.ui.theme.Hall_finderTheme
import androidx.compose.foundation.layout.*
import com.example.hall_finder.model.MapData
import com.example.hall_finder.model.Node
import android.util.Log
import androidx.compose.runtime.LaunchedEffect

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme{
                MapScreen()
            }
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun MapScreen() {

    LaunchedEffect(Unit) {
        val neighbors = MapData.graph.getNeighbors("n2")
        Log.d("GRAPH TEST", neighbors.toString())
    }

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
        }
    }
}