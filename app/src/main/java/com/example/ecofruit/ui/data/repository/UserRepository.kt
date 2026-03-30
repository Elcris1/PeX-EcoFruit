package com.example.ecofruit.ui.data.repository

import android.util.Log
import com.example.ecofruit.ui.data.mock.MockData
import com.example.ecofruit.ui.data.model.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID


class UserRepository private constructor() {
    private val TAG = "UserRepository"

    private val _users = MutableStateFlow<List<User>>(emptyList())

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    init {
        _users.value = MockData.users
    }

    fun getUserById(userId: String): User? {
        return _users.value.find { it.id == userId }
    }

    fun getUserByEmail(email: String): User? {
        return _users.value.find { it.email == email }
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

    suspend fun registerUser(name: String, email: String, password: String): Result<User>{
        Log.d(TAG, "Registering user")
        delay(1000)
        var user: User? = null
        if (getUserByEmail(email) == null) {
            user = User(
                id = UUID.randomUUID().toString(),
                name = name,
                email = email,
                createdAt = System.currentTimeMillis(),
                profileImageUrl = "",
                bio = "",
                isProducer = false,
                followers = 0,
                following = emptyList(),
                reviewCount = 0,
                rating = 0.0,
                location = null
            )
            Log.d(TAG, "User created ${user}")
            _users.update { currentUsers ->
                currentUsers + user
            }
        }
        if (user!= null){
            _user.value = user
            return Result.success(user)
        } else {
            return Result.failure(Exception("Error creando usuario, email ya en uso"))
        }

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