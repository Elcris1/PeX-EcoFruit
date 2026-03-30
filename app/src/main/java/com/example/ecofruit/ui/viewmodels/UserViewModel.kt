package com.example.ecofruit.ui.viewmodels

import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecofruit.ui.data.model.RequestUiState
import com.example.ecofruit.ui.data.model.User
import com.example.ecofruit.ui.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserViewModel(
    private val userRepo: UserRepository
) : ViewModel() {

    val currentUser: StateFlow<User?> = userRepo.user

    private val _uiState = MutableStateFlow<RequestUiState<User>>(RequestUiState.Idle())
    val uiState: StateFlow<RequestUiState<User>> = _uiState.asStateFlow()

    private val _registerUiState = MutableStateFlow<RequestUiState<User>>(RequestUiState.Idle())
    val registerUiState:  StateFlow<RequestUiState<User>> = _registerUiState.asStateFlow()


    fun logUserIn(email: String, password: String) {

        viewModelScope.launch {
            _uiState.value = RequestUiState.Loading()
            userRepo.login(email, password)
                .onSuccess { _uiState.value = RequestUiState.Success(it) }
                .onFailure { _uiState.value = RequestUiState.Error(it.message ?: "Error") }
        }
    }

    fun logOut() {
        userRepo.logOut()
    }

    fun registerUser(name: String, email: String, password: String) {
        viewModelScope.launch {
            _registerUiState.value = RequestUiState.Loading()
            userRepo.registerUser(name, email, password)
                .onSuccess { _registerUiState.value = RequestUiState.Success(it) }
                .onFailure { _registerUiState.value = RequestUiState.Error(it.message ?: "Error") }
        }
    }

}
