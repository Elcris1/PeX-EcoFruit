package com.example.ecofruit.ui.data.mock

import com.example.ecofruit.ui.data.constants.ProductType
import com.example.ecofruit.ui.data.constants.ProductUnit
import com.example.ecofruit.ui.data.model.Product

object ProductsMockData {
    val mockProducts = listOf(
        Product(
            id = "p1",
            name = "Manzanas Ecológicas",
            description = "Manzanas frescas cultivadas sin pesticidas.",
            createdAt = System.currentTimeMillis() - 86400000L * 2,
            imagesUrl = listOf(
                "https://images.unsplash.com/photo-1567306226416-28f0efdc88ce",
                "https://images.unsplash.com/photo-1570913149827-d2ac84ab3f9a"
            ),
            price = 2.99,
            unit = ProductUnit.KG,
            userId = "u1",
            userName = "Carlos Pérez",
            userAvatar = "https://i.pravatar.cc/150?img=1",
            favouritesList = listOf("u2", "u3"),
            rating = 4.8,
            reviewCount = 21,
            isOrganic = true,
            type = ProductType.FRUITS
        ),
        Product(
            id = "p2",
            name = "Tomates Cherry",
            description = "Tomates dulces y jugosos, ideales para ensaladas.",
            createdAt = System.currentTimeMillis() - 86400000L * 1,
            imagesUrl = listOf(
                "https://images.unsplash.com/photo-1570543375343-63fe3d67761b"
            ),
            price = 3.49,
            unit = ProductUnit.KG,
            userId = "u3",
            userName = "Miguel Torres",
            userAvatar = "https://i.pravatar.cc/150?img=8",
            favouritesList = listOf("u1"),
            rating = 4.5,
            reviewCount = 10,
            isOrganic = false,
            type = ProductType.FRUITS
        ),
        Product(
            id = "p3",
            name = "Leche Fresca",
            description = "Leche recién ordeñada de vacas alimentadas con pasto.",
            createdAt = System.currentTimeMillis() - 86400000L * 3,
            imagesUrl = listOf(
                "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b"
            ),
            price = 1.20,
            unit = ProductUnit.LITER,
            userId = "u1",
            userName = "Carlos Pérez",
            userAvatar = "https://i.pravatar.cc/150?img=1",
            favouritesList = emptyList(),
            rating = 4.2,
            reviewCount = 5,
            isOrganic = false,
            type = ProductType.FROM_ANIMAL
        ),
        Product(
            id = "p4",
            name = "Huevos Camperos",
            description = "Docena de huevos de gallinas criadas en libertad.",
            createdAt = System.currentTimeMillis() - 86400000L * 5,
            imagesUrl = listOf(
                "https://images.unsplash.com/photo-1582722872445-44dc5f7e3c8f"
            ),
            price = 2.80,
            unit = ProductUnit.UNIT,
            userId = "u3",
            userName = "Miguel Torres",
            userAvatar = "https://i.pravatar.cc/150?img=8",
            favouritesList = listOf("u2"),
            rating = 4.6,
            reviewCount = 14,
            isOrganic = false,
            type = ProductType.FROM_ANIMAL
        )
    )
    val extraProducts = listOf(
        Product(
            id = "p5",
            name = "Zanahorias Orgánicas",
            description = "Crujientes y dulces, recién recolectadas.",
            createdAt = System.currentTimeMillis() - 86400000L * 4,
            imagesUrl = listOf("https://images.unsplash.com/photo-1586626205306-07e752385d35"),
            price = 1.80,
            unit = ProductUnit.KG,
            userId = "u3",
            userName = "Miguel Torres",
            userAvatar = "https://i.pravatar.cc/150?img=8",
            favouritesList = listOf("u1", "u2"),
            rating = 4.4,
            reviewCount = 9,
            isOrganic = true,
            type = ProductType.VEGETABLES
        ),
        Product(
            id = "p6",
            name = "Pan Artesanal",
            description = "Pan hecho a mano con masa madre.",
            createdAt = System.currentTimeMillis() - 86400000L * 1,
            imagesUrl = listOf("https://images.unsplash.com/photo-1608198093002-ad4e005484ec"),
            price = 2.50,
            unit = ProductUnit.UNIT,
            userId = "u2",
            userName = "Laura Gómez",
            userAvatar = "https://i.pravatar.cc/150?img=5",
            favouritesList = listOf("u1"),
            rating = 4.9,
            reviewCount = 40,
            isOrganic = false,
            type = ProductType.CEREAL
        ),
        Product(
            id = "p7",
            name = "Queso Curado",
            description = "Queso artesanal con 6 meses de curación.",
            createdAt = System.currentTimeMillis() - 86400000L * 6,
            imagesUrl = listOf("https://images.unsplash.com/photo-1486297678162-eb2a19b0a32d"),
            price = 5.75,
            unit = ProductUnit.KG,
            userId = "u1",
            userName = "Carlos Pérez",
            userAvatar = "https://i.pravatar.cc/150?img=1",
            favouritesList = listOf("u2", "u3"),
            rating = 4.7,
            reviewCount = 18,
            isOrganic = false,
            type = ProductType.FROM_ANIMAL
        )
    )
    val allProducts = mockProducts + extraProducts
}