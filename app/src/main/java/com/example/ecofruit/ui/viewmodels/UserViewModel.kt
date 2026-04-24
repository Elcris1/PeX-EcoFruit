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

    fun logOut() {
        userRepo.logOut()
    }


    private val _producerState = MutableStateFlow<RequestUiState<Unit>>(RequestUiState.Idle())
    val producerState = _producerState.asStateFlow()

    fun changeProducerState() {
        _producerState.value = RequestUiState.Loading()
        viewModelScope.launch {
            userRepo.changeProducerState().onSuccess {
                _producerState.value = RequestUiState.Success(Unit)
            }.onFailure {
                _producerState.value = RequestUiState.Error(it.message ?: "Error al convertir a productor")
            }
        }
    }

    fun followUser(userId: String) {
        userRepo.followUser(userId)
    }

    fun unfollowUser(userId: String) {
        userRepo.unfollowUser(userId)
    }

}
