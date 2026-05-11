package com.example.ecofruit.ui.viewmodels

import android.net.Uri
import android.util.Log
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

    private val _updateState = MutableStateFlow<RequestUiState<Unit>>(RequestUiState.Idle())
    val updateState: StateFlow<RequestUiState<Unit>> = _updateState.asStateFlow()

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

    fun updateUser(updatedUser: User, imageUri: Uri?) {
        _updateState.value = RequestUiState.Loading()
        viewModelScope.launch {
            userRepo.updateUser(updatedUser, imageUri).onSuccess {
                _updateState.value = RequestUiState.Success(Unit)
            }.onFailure {
                _updateState.value = RequestUiState.Error(it.message ?: "Error al actualizar perfil")
            }
        }
    }

    fun setProducerNotificationPreference(token: String, producersNotification: Boolean) {
        viewModelScope.launch {
            userRepo.updateFcmTokenProducerPreference(token, producersNotification)
        }
    }

    fun resetUpdateState() {
        _updateState.value = RequestUiState.Idle()
    }

    fun followUser(userId: String) {
        viewModelScope.launch {
            userRepo.followUser(userId).onFailure {
                Log.e("UserViewModel", "Error following user: ${it.message}")
            }
        }
    }

    fun unfollowUser(userId: String) {
        viewModelScope.launch {
            userRepo.unfollowUser(userId).onFailure {
                Log.e("UserViewModel", "Error unfollowing user: ${it.message}")
            }
        }
    }

}
