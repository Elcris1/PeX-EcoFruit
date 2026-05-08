package com.example.ecofruit.ui.viewmodels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecofruit.ui.data.model.RequestUiState
import com.example.ecofruit.ui.data.model.User
import com.example.ecofruit.ui.data.repository.AuthRepository
import com.example.ecofruit.ui.data.repository.SettingsRepository
import com.example.ecofruit.ui.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel (
    private val authRepo: AuthRepository = AuthRepository(),
    private val userRepo: UserRepository = UserRepository.getInstance(),
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    var user by mutableStateOf<FirebaseUser?>(auth.currentUser)
        private set

    // Atributo para el modelo de usuario de la aplicación, sincronizado con el repositorio singleton
    var currentAppUserModel by mutableStateOf<User?>(userRepo.user.value)
        private set

    private val _uiState = MutableStateFlow<RequestUiState<FirebaseUser>>(RequestUiState.Idle())
    val uiState = _uiState.asStateFlow()
    
    private val _registerUiState = MutableStateFlow<RequestUiState<FirebaseUser>>(RequestUiState.Idle())
    val registerUiState = _registerUiState.asStateFlow()


    private val _resetPasswordState = MutableStateFlow<RequestUiState<Unit>>(RequestUiState.Idle())
    val resetPasswordState = _resetPasswordState.asStateFlow()

    init {
        // Observamos el StateFlow del repositorio para mantener este ViewModel actualizado
        viewModelScope.launch {
            userRepo.user.collect { updatedUser ->
                currentAppUserModel = updatedUser
            }
        }
        checkSession()
    }

    private fun checkSession() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            _uiState.value = RequestUiState.Loading()
            viewModelScope.launch {
                // reloadUser ya actualiza el StateFlow en el repositorio
                userRepo.reloadUser().onSuccess { appUser ->
                    if (appUser != null) {
                        _uiState.value = RequestUiState.Success(currentUser)
                    } else {
                        _uiState.value = RequestUiState.Idle()
                    }
                }.onFailure {
                    _uiState.value = RequestUiState.Error(it.message ?: "Error al recuperar sesión")
                }
            }
        }
    }

    fun login(email: String, password: String) {
        _uiState.value = RequestUiState.Loading()
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    user = firebaseUser
                    viewModelScope.launch {
                        if (firebaseUser != null) {
                            // getUserFromFirestore ya actualiza el StateFlow en el repositorio
                            userRepo.getUserFromFirestore(firebaseUser.uid).onSuccess {
                                _uiState.value = RequestUiState.Success(firebaseUser)
                            }.onFailure {
                                _uiState.value = RequestUiState.Error(it.message ?: "Error al cargar perfil")
                            }
                        }
                    }
                } else {
                    _uiState.value = RequestUiState.Error(task.exception?.message ?: "Login fallido")
                }
            }
    }

    fun register(name: String, email: String, password: String) {
        _registerUiState.value = RequestUiState.Loading()
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    if (firebaseUser != null) {
                        val newUser = User(
                            id = firebaseUser.uid,
                            name = name,
                            email = email,
                            createdAt = System.currentTimeMillis()/1000,
                            profileImageUrl = "",
                            bio = "",
                            location = null,
                            isProducer = false,
                            following = emptyList(),
                            followers = 0,
                            reviewCount = 0,
                            rating = 0.0
                        )
                        firebaseUser.sendEmailVerification()
                        viewModelScope.launch {
                            // createUserInFirestore ya actualiza el StateFlow en el repositorio
                            userRepo.createUserInFirestore(newUser).onSuccess {
                                user = firebaseUser
                                _registerUiState.value  = RequestUiState.Success(firebaseUser)
                            }.onFailure {
                                _registerUiState.value = RequestUiState.Error(it.message ?: "Error al crear perfil en Firestore")
                            }
                        }
                    }
                } else {
                    _registerUiState.value = RequestUiState.Error(task.exception?.message ?: "Registro fallido")
                }
            }
    }

    fun signInWithGoogle(idToken: String) {
        _uiState.value = RequestUiState.Loading()
        viewModelScope.launch {
            authRepo.signInWithGoogle(idToken).onSuccess { firebaseUser ->
                userRepo.getUserFromFirestore(firebaseUser.uid).onSuccess { existingUser ->
                    if (existingUser == null) {
                        val newUser = User(
                            id = firebaseUser.uid,
                            name = firebaseUser.displayName ?: "Usuario Google",
                            email = firebaseUser.email ?: "",
                            createdAt = System.currentTimeMillis()/1000,
                            profileImageUrl = firebaseUser.photoUrl?.toString() ?: "",
                            bio = "",
                            location = null,
                            isProducer = false,
                            following = emptyList(),
                            followers = 0,
                            reviewCount = 0,
                            rating = 0.0
                        )
                        userRepo.createUserInFirestore(newUser)
                    }
                    user = firebaseUser
                    _uiState.value = RequestUiState.Success(firebaseUser)
                }.onFailure {
                    _uiState.value = RequestUiState.Error("Error al verificar perfil")
                }
            }.onFailure {
                _uiState.value = RequestUiState.Error(it.message ?: "Login con Google fallido")
            }
        }
    }

    fun sendPasswordResetEmail(email: String) {
        _resetPasswordState.value = RequestUiState.Loading()
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _resetPasswordState.value = RequestUiState.Success(Unit)
                } else {
                    _resetPasswordState.value = RequestUiState.Error(task.exception?.message ?: "Error al enviar correo")
                }
            }
    }

    fun clearResetPasswordState() {
        _resetPasswordState.value = RequestUiState.Idle()
    }

    fun logout() {
        viewModelScope.launch {
            try {
                settingsRepository.settingsFlow.collect { settings ->
                    settings.fcmToken?.let {
                        userRepo.updateFcmTokenStatus(settings.fcmToken, false)
                    }
                }
            } catch (exception: Exception ) {
                Log.d("AuthViewModel", "Error al actualizar estado del token FCM: ${exception.message}")
            } finally {
                userRepo.logOut()
                user = null
                _uiState.value = RequestUiState.Idle()
            }
        }

    }
}
