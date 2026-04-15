package com.example.hall_finder.graph

import com.example.hall_finder.model.Node
import com.example.hall_finder.model.Edge
import com.example.hall_finder.model.EdgeType
import kotlin.math.sqrt

class Graph(private val nodes: List<Node>) {
    private val adjacencyList = mutableMapOf<String, MutableList<Edge>>()

    init {
        nodes.forEach { adjacencyList[it.id] = mutableListOf() }
    }

    fun addBidirectionalEdge(from: String, to: String, type: EdgeType = EdgeType.NORMAL) {
        val fromNode = findNode(from)
        val toNode = findNode(to)
        val distance = calculateDistance(fromNode, toNode)

        adjacencyList[from]?.add(Edge(from, to, distance, type))
        adjacencyList[to]?.add(Edge(to, from, distance, type))
    }

    fun getNeighbors(nodeId: String, accessibleOnly: Boolean = false): List<Edge> {
        val edges = adjacencyList[nodeId] ?: emptyList()
        return if (accessibleOnly) {
            edges.filter { it.type != EdgeType.STAIRS }
        } else {
            edges
        }
    }

    private fun findNode(id: String): Node {
        return nodes.first { it.id == id }
    }

    private fun calculateDistance(a: Node, b: Node): Float {
        val dx = a.x - b.x
        val dy = a.y - b.y
        return sqrt(dx * dx + dy * dy)
    }
}