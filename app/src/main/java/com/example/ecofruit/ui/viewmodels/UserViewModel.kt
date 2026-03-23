package com.example.ecofruit.ui.viewmodels

import com.example.ecofruit.ui.data.model.User
import com.example.ecofruit.ui.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class UserViewModel {
    private val userRepo = UserRepository()
    private var _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    fun logUserIn(email: String, password: String): Boolean {
        val user = userRepo.getUserByEmail(email)

        user?.let {
            _user.value = user
            return true
        }

        return false
    }
}