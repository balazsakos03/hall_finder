package com.example.hall_finder.model

enum class EdgeType {
    NORMAL,
    STAIRS,
    ELEVATOR
}

data class Edge(
    val from: String,
    val to: String,
    val weight: Float,
    val type: EdgeType = EdgeType.NORMAL
)
