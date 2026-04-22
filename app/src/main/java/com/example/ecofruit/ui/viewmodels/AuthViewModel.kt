package com.example.ecofruit.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecofruit.ui.data.model.RequestUiState
import com.example.ecofruit.ui.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel (
    private val repo: AuthRepository = AuthRepository()
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    var user by mutableStateOf<FirebaseUser?>(auth.currentUser)
        private set

    private val _uiState = MutableStateFlow<RequestUiState<FirebaseUser>>(RequestUiState.Idle())
    val uiState = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        _uiState.value = RequestUiState.Loading()
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    user = auth.currentUser
                    user?.let { _uiState.value = RequestUiState.Success(it) }
                } else {
                    _uiState.value = RequestUiState.Error(task.exception?.message ?: "Login fallido")
                }
            }
    }

    fun register(email: String, password: String) {
        _uiState.value = RequestUiState.Loading()
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    user = auth.currentUser
                    user?.let { _uiState.value = RequestUiState.Success(it) }
                } else {
                    _uiState.value = RequestUiState.Error(task.exception?.message ?: "Registro fallido")
                }
            }
    }

    fun signInWithGoogle(idToken: String) {
        _uiState.value = RequestUiState.Loading()
        viewModelScope.launch {
            repo.signInWithGoogle(idToken).onSuccess {
                user = it
                _uiState.value = RequestUiState.Success(it)
            }.onFailure {
                _uiState.value = RequestUiState.Error(it.message ?: "Login con Google fallido")
            }
        }
    }

    fun logout() {
        auth.signOut()
        user = null
        _uiState.value = RequestUiState.Idle()
    }
}
