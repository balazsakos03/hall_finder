package com.example.hall_finder.graph

import com.example.hall_finder.model.Node
import com.example.hall_finder.model.Edge
import kotlin.math.sqrt

class Graph(private val nodes: List<Node>) {
    private val adjacencyList = mutableMapOf<String, MutableList<Edge>>()

    init {
        nodes.forEach { adjacencyList[it.id] = mutableListOf() }
    }

    fun addBidirectionalEdge(from: String, to: String){
        val fromNode = findNode(from)
        val toNode = findNode(to)

        val distance = calculateDistance(fromNode, toNode)

        adjacencyList[from]?.add(Edge(from, to, distance))
        adjacencyList[to]?.add(Edge(to, from, distance))
    }

    fun getNeighbors(nodeId: String): List<Edge>{
        return adjacencyList[nodeId] ?: emptyList()
    }

    private fun findNode(id: String): Node{
        return nodes.first{it.id == id}
    }

    private fun calculateDistance(a: Node, b: Node): Float{
        val dx = a.x - b.x
        val dy = a.y - b.y
        return sqrt(dx*dx + dy*dy)
    }
}