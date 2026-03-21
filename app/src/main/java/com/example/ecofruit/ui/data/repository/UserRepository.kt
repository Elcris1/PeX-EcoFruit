package com.example.ecofruit.ui.data.repository

import com.example.ecofruit.ui.data.mock.MockData
import com.example.ecofruit.ui.data.model.User

class UserRepository {
    fun getUserById(userId: String): User? {
        return MockData.users.find { it.id == userId }
    }

    fun getUserByEmail(email: String): User? {
        return MockData.users.find { it.email == email }
    }
}