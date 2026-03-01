package com.example.hall_finder.graph

import androidx.compose.ui.graphics.rememberGraphicsLayer
import com.example.hall_finder.model.MapData.graph
import com.example.hall_finder.model.MapData.nodes
import com.example.hall_finder.model.Node
import kotlin.math.sqrt
import java.util.PriorityQueue

class AStar (private val graph: Graph, private val nodes: List<Node>){
    fun findPath(startId: String, goalId: String): List<String> {
        val openSet = PriorityQueue(compareBy<Pair<String, Float>> {it.second})
        openSet.add(startId to 0f)

        val cameFrom = mutableMapOf<String, String>()

        val gScore = mutableMapOf<String, Float>().withDefault { Float.POSITIVE_INFINITY }
        gScore[startId] = 0f

        val fScore = mutableMapOf<String, Float>().withDefault { Float.POSITIVE_INFINITY }
        fScore[startId] = heuristic(startId, goalId)

        while (openSet.isNotEmpty()){
            val current = openSet.poll().first

            if (current == goalId){
                return reconstructPath(cameFrom, current)
            }

            for (edge in graph.getNeighbors(current)){
                val tentativeG = gScore.getValue(current) + edge.weight

                if(tentativeG < gScore.getValue(edge.to)){
                    cameFrom[edge.to] = current
                    gScore[edge.to] = tentativeG
                    fScore[edge.to] = tentativeG + heuristic(edge.to, goalId)

                    openSet.add(edge.to to fScore[edge.to]!!)
                }
            }
        }
        return emptyList()
    }

    private fun heuristic(aId: String, bId: String): Float{
        val a = nodes.first {it.id == aId}
        val b = nodes.first {it.id == bId}

        val dx = a.x - b.x
        val dy = a.y - b.y
        return sqrt(dx*dx + dy*dy)
    }

    private fun reconstructPath(cameFrom: Map<String, String>, current: String): List<String>{
        val totalPath = mutableListOf(current)
        var curr = current

        while(cameFrom.containsKey(curr)){
            curr = cameFrom[curr]!!
            totalPath.add(curr)
        }

        return totalPath.reversed()
    }
}