package com.example.ecofruit.ui.data.repository

import com.example.ecofruit.ui.data.mock.MockData
import com.example.ecofruit.ui.data.model.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


class UserRepository private constructor() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    fun getUserById(userId: String): User? {
        return MockData.users.find { it.id == userId }
    }

    fun getUserByEmail(email: String): User? {
        return MockData.users.find { it.email == email }
    }

    suspend fun login(email: String, password: String): Result<User> {
        delay(1000) // Simula latencia de red
        val user = getUserByEmail(email)

        return if (user != null) {
            _user.value = user
            Result.success(user)
        } else {
            Result.failure(Exception("Credenciales incorrectas"))
        }
    }
    fun logOut() {
        _user.value = null
    }

    //Patron singleton
    companion object {
        @Volatile
        private var INSTANCE: UserRepository? = null

        fun getInstance(): UserRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserRepository().also { INSTANCE = it }
            }
        }
    }
}