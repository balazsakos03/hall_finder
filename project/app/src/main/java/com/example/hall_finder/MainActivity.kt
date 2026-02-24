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

data class Node(
    val id: String,
    val x: Float,
    val y: Float
)

val nodes = listOf(
    Node("n1", 56.5f, 1612.5f),
    Node("n2", 536.5f, 1612.5f),
    Node("n3", 857.5f, 1612.5f),
    Node("n4", 536.5f, 907.5f),
    Node("n5", 536.5f, 277.5f),
    Node("n6", 863.5f, 277.5f),
    Node("n7", 252.5f, 1570.5f),
    Node("n8", 811.5f, 1790.5f),
    Node("n9", 586.5f, 1414.5f),
    Node("n10", 586.5f, 1163f),
    Node("n11", 252.5f, 957.5f),
    Node("n12", 252.5f, 857.5f),
    Node("n13", 586.5f, 510.5f),
    Node("n14", 255.5f, 323.5f),
    Node("n15", 863.5f, 624.5f),
    Node("n16", 588.5f, 102.5f),
    Node("n17", 486.5f, 102.5f)
)

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun MapScreen() {

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
                nodes.forEach { node ->
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