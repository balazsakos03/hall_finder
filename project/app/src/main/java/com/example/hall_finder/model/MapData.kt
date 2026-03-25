package com.example.hall_finder.model

import com.example.hall_finder.graph.Graph

object MapData {
    val nodes = listOf(
        //foldszint
        Node("n1", 56.5f, 1612.5f, 1),
        Node("n2", 536.5f, 1612.5f, 1),
        Node("n3", 857.5f, 1612.5f, 1),
        Node("n4", 536.5f, 907.5f, 1),
        Node("n5", 536.5f, 277.5f, 1),
        Node("n6", 863.5f, 277.5f, 1),
        Node("n7", 252.5f, 1570.5f, 1),
        Node("n8", 811.5f, 1790.5f, 1),
        Node("n9", 586.5f, 1414.5f, 1),
        Node("n10", 586.5f, 1163f, 1),
        Node("n11", 252.5f, 957.5f, 1),
        Node("n12", 252.5f, 857.5f, 1),
        Node("n13", 586.5f, 510.5f, 1),
        Node("n14", 255.5f, 323.5f, 1),
        Node("n15", 863.5f, 624.5f, 1),
        Node("n16", 588.5f, 102.5f, 1),
        Node("n17", 486.5f, 102.5f, 1),

        //foldszinti seged nodeok
        Node("node_A", 252.5f, 1612.5f, 1),
        Node("node_B", 857.5f, 1790.5f, 1),
        Node("node_C", 536.5f, 1414.5f, 1),
        Node("node_D", 536.5f, 1163.5f, 1),
        Node("node_E", 252.5f, 907.5f, 1),
        Node("node_F", 536.5f, 510.5f, 1),
        Node("node_G", 255.5f, 277.5f, 1),
        Node("node_H", 536.5f, 102.5f, 1),

        //foldszinti lepcsok es lift
        Node("n18", 903.5f, 1791.5f, 1), // lepcso 1
        Node("n19", 908.5f, 278.5f, 1),  // lepcso 2
        Node("n20", 160.5f, 281.5f, 1),  // lift

        //2. emelet
        //emeleti atjarok
        Node("n21", 903.5f, 1791.5f, 2), // lepcso 1
        Node("n22", 908.5f, 278.5f, 2),  // lepcso 2
        Node("n23", 161.5f, 278.5f, 2),  // lift

        //emeleti folyosok es termek
        Node("n24", 858.5f, 1791.5f, 2), // folyoso vege
        Node("n25", 858.5f, 1613.5f, 2), // elagazas
        Node("n26", 656.5f, 1613.5f, 2), // kisegito node
        Node("n27", 656.5f, 1654.5f, 2), // nagy targyalo bejarat
        Node("n28", 537.5f, 1613.5f, 2), // elagazas
        Node("n29", 331.5f, 1613.5f, 2), // kisegito node
        Node("n30", 331.5f, 1654.5f, 2), // kis targyalo bejarat
        Node("n31", 247.5f, 1613.5f, 2), // folyoso vege
        Node("n32", 247.5f, 1571.5f, 2), // 2. bufe bejarat
        Node("n33", 537.5f, 1415.5f, 2), // kisegito node
        Node("n34", 587.5f, 1415.5f, 2), // 5. iroda bejarat
        Node("n35", 537.5f, 1167.5f, 2), // kisegito node
        Node("n36", 587.5f, 1167.5f, 2), // 6. iroda bejarat
        Node("n37", 537.5f, 1030.5f, 2), // elagazas
        Node("n38", 256.5f, 1030.5f, 2), // folyoso vege
        Node("n39", 256.5f, 1084.5f, 2), // hr bejarat
        Node("n40", 256.5f, 980.5f, 2),  // szerver bejarat
        Node("n41", 537.5f, 742.5f, 2),  // elagazas
        Node("n42", 819.5f, 742.5f, 2),  // folyoso vege
        Node("n43", 819.5f, 793.5f, 2),  // piheno/konyha bejarat
        Node("n44", 819.5f, 692.5f, 2),  // nyomtato allomas bejarat
        Node("n45", 537.5f, 287.5f, 2),  // elagazas
        Node("n46", 819.5f, 278.5f, 2),  // folyoso vege
        Node("n47", 819.5f, 331.5f, 2),  // 3. raktar bejarat
        Node("n48", 210.5f, 278.5f, 2),  // folyoso vege
        Node("n49", 210.5f, 493.5f, 2),  // noi mosdo bejarat
        Node("n50", 537.5f, 68.5f, 2),   // folyoso vege
        Node("n51", 487.5f, 68.5f, 2),   // vezetoi iroda bejarat
        Node("n52", 724.5f, 68.5f, 2)    // ferfi mosdo bejarat
    )

    val graph = Graph(nodes).apply {
        //foldszint elei
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
        addBidirectionalEdge("n5", "n6")
        addBidirectionalEdge("n5", "node_H")

        //foldszinti termek bekotese
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

        //foldszinti lift es lepcsok bekotese a folyosokra
        addBidirectionalEdge("node_B", "n18") // Lépcső 1
        addBidirectionalEdge("n6", "n19")     // Lépcső 2
        addBidirectionalEdge("node_G", "n20") // Lift


        //teleportok(emeletek osszekotese)
        addBidirectionalEdge("n18", "n21") // Lépcső 1: 1. emelet <-> 2. emelet
        addBidirectionalEdge("n19", "n22") // Lépcső 2: 1. emelet <-> 2. emelet
        addBidirectionalEdge("n20", "n23") // Lift: 1. emelet <-> 2. emelet


        //2. emelet elei
        addBidirectionalEdge("n21", "n24")
        addBidirectionalEdge("n24", "n25")
        addBidirectionalEdge("n25", "n26")
        addBidirectionalEdge("n26", "n28")
        addBidirectionalEdge("n26", "n27")
        addBidirectionalEdge("n28", "n33")
        addBidirectionalEdge("n28", "n29")
        addBidirectionalEdge("n29", "n31")
        addBidirectionalEdge("n29", "n30")
        addBidirectionalEdge("n31", "n32")
        addBidirectionalEdge("n33", "n34")
        addBidirectionalEdge("n33", "n35")
        addBidirectionalEdge("n35", "n36")
        addBidirectionalEdge("n35", "n37")
        addBidirectionalEdge("n37", "n38")
        addBidirectionalEdge("n37", "n41")
        addBidirectionalEdge("n38", "n40")
        addBidirectionalEdge("n38", "n39")
        addBidirectionalEdge("n41", "n42")
        addBidirectionalEdge("n41", "n45")
        addBidirectionalEdge("n42", "n44")
        addBidirectionalEdge("n42", "n43")
        addBidirectionalEdge("n45", "n46")
        addBidirectionalEdge("n45", "n48")
        addBidirectionalEdge("n45", "n50")
        addBidirectionalEdge("n46", "n22")
        addBidirectionalEdge("n46", "n47")
        addBidirectionalEdge("n48", "n23")
        addBidirectionalEdge("n48", "n49")
        addBidirectionalEdge("n50", "n51")
        addBidirectionalEdge("n50", "n52")
    }
}