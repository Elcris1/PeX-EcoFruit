package com.example.ecofruit.ui.data.mock

import com.example.ecofruit.ui.data.model.Product
import com.example.ecofruit.ui.data.model.User

object MockData {
    val users = listOf(
        User("u1", "Eloi Consumidor", "eloiconsumidor@email.com","", false),
        User("u2", "Eloi Productor", "eloiproductor@email.com","", true),
        User("u3", "Consumidor1", "consumidor1@email.com","", false),
        User("u4","Productor with Image", "productor1@email.com",
            "https://img.freepik.com/vector-premium/granjero-dibujos-animados-pie-hierba-verde_1305385-8640.jpg?semt=ais_hybrid&w=740&q=80",
            true
        )
    )

    val products = listOf(
        Product("p1","Product 1","Product without image","", 10.0, "u2"),
        Product("p2", "non-user-product", "description", "", 0.1, ""),
        Product("p3", "Product 2", "Product with image and profile image",
            "https://www.bupasalud.com/sites/default/files/inline-images/fuji-red.jpg",
            10.0, "u4")
    )
}