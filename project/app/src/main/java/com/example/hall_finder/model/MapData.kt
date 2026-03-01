package com.example.hall_finder.model

import com.example.hall_finder.graph.Graph

object MapData {
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
        Node("n17", 486.5f, 102.5f),

        //uj folyosoi nodeok, segitseg a bejaratoknal
        Node("node_A", 252.5f, 1612.5f),
        Node("node_B", 857.5f, 1790.5f),
        Node("node_C", 536.5f, 1414.5f),
        Node("node_D", 536.5f, 1163.5f),
        Node("node_E", 252.5f, 907.5f),
        Node("node_F", 536.5f, 510.5f),
        Node("node_G", 255.5f, 277.5f),
        Node("node_H", 536.5f, 102.5f)
    )

    val graph = Graph(nodes).apply {
        //folyoso gerince
        addBidirectionalEdge("n1", "node_A")
        addBidirectionalEdge("node_A", "n2")
        addBidirectionalEdge("n2", "n3")
        addBidirectionalEdge("n3", "node_B")

        addBidirectionalEdge("n2", "node_C")
        addBidirectionalEdge("node_C", "node_D")
        addBidirectionalEdge("node_D", "n4")

        addBidirectionalEdge("n4", "node_E")
        addBidirectionalEdge("n4", "node_F")

        addBidirectionalEdge("node_F", "n5")
        addBidirectionalEdge("n5", "node_G")
        addBidirectionalEdge("n5", "n5")
        addBidirectionalEdge("n5", "node_H")

        //termek
        addBidirectionalEdge("n7", "node_A")
        addBidirectionalEdge("n8", "node_B")
        addBidirectionalEdge("n9", "node_C")
        addBidirectionalEdge("n10", "node_D")
        addBidirectionalEdge("n12", "node_E")
        addBidirectionalEdge("n11", "node_E")
        addBidirectionalEdge("n13", "node_F")
        addBidirectionalEdge("n14", "node_G")
        addBidirectionalEdge("n15", "n6")
        addBidirectionalEdge("n16", "node_H")
        addBidirectionalEdge("n17", "node_H")
    }
}