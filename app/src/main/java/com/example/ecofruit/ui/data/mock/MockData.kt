package com.example.ecofruit.ui.data.mock

import com.example.ecofruit.ui.data.constants.ReviewType
import com.example.ecofruit.ui.data.model.Product
import com.example.ecofruit.ui.data.model.Review
import com.example.ecofruit.ui.data.model.User

object MockData {
    val users = listOf(
        User(
            id="u1",
            name="Eloi Consumidor",
            email="eloiconsumidor@email.com",
            profileImageUrl = "",
            isProducer = false,
            createdAt = 0,
            bio = "",
            location = null,
            followers = 0,
            following = listOf("u2", "u4"),
            reviewCount = 0,
            rating = 0.0
        ),
        User(
            id="u2",
            name="Eloi Productor",
            email="eloiproductor@email.com",
            profileImageUrl = "",
            isProducer = true,
            createdAt = 0,
            bio = "",
            location = null,
            followers = 2,
            following = emptyList(),
            reviewCount = 2,
            rating = 4.5
        ),
        User(
            id="u3",
            name="Consumidor1",
            email="consumidor1@email.com",
            profileImageUrl = "",
            isProducer = false,
            createdAt = 0,
            bio = "",
            location = null,
            followers = 0,
            following = listOf("u2"),
            reviewCount = 0,
            rating = 0.0
        ),
        User(
            id="u4",
            name="Productor with Image",
            email="productor1@email.com",
            profileImageUrl = "https://img.freepik.com/vector-premium/granjero-dibujos-animados-pie-hierba-verde_1305385-8640.jpg?semt=ais_hybrid&w=740&q=80",
            isProducer = true,
            createdAt = 0,
            bio = "Producing local products since i was 20",
            location = null,
            followers = 1,
            following = emptyList(),
            reviewCount = 1,
            rating = 5.0
        ),
        ChatMockData.currentUser,
        ChatMockData.marta,
        ChatMockData.laia,
        ChatMockData.pau,
        ChatMockData.jordi
    )

    val products = listOf(
        Product(
            id = "p1",
            name = "Product 1",
            description = "Product without image",
            imagesUrl = emptyList(),
            price = 10.0,
            userId = "u2",
            userName = "Eloi Productor",
            userAvatar =  ""
        ),
        Product(
            id = "p2",
            name = "non-user-product",
            description = "description",
            imagesUrl = emptyList(),
            price = 10.0,
            userId = "",
            userName = "",
            userAvatar = ""
        ),
        Product(
            id = "p3",
            name = "Product 2",
            description = "Product with image and profile image",
            imagesUrl = listOf("https://www.bupasalud.com/sites/default/files/inline-images/fuji-red.jpg"),
            price = 10.0,
            userId = "u4",
            userName = "Productor with Image",
            userAvatar = "https://img.freepik.com/vector-premium/granjero-dibujos-animados-pie-hierba-verde_1305385-8640.jpg?semt=ais_hybrid&w=740&q=80"
        ),

    )
    val reviews = listOf(
        Review(
            id = "r1",
            userId = "u1",
            dstId = "u2",
            reviewType = ReviewType.USER,
            authorName = "Eloi Consumidor",
            authorAvatar = "",
            rating = 5.0,
            title = "Great Productor",
            description = "Kind and fast",
            createdAt = 0
        ),
        Review(
            id = "r2",
            userId = "u3",
            dstId = "u2",
            reviewType = ReviewType.USER,
            authorName = "Consumidor1",
            authorAvatar = "",
            rating = 4.0,
            title = "One of the best",
            description = "Good job during all the process",
            createdAt = 0
        ),
        Review(
            id = "r2",
            userId = "u1",
            dstId = "u4",
            reviewType = ReviewType.USER,
            authorName = "Eloi Consumidor",
            authorAvatar = "",
            rating = 4.0,
            title = "Always great!",
            description = "One of the best productors ive ever talked to",
            createdAt = 0
        ),
    )
}