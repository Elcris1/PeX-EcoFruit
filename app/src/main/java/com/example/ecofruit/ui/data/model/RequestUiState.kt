package com.example.ecofruit.ui.data.model

sealed class RequestUiState<T> {
    class Idle<T> : RequestUiState<T>()
    class Loading<T> : RequestUiState<T>()
    class Empty<T> : RequestUiState<T>()

    data class Success<T>(
        val data: T
    ) : RequestUiState<T>()
    data class Error<T>(val message: String) : RequestUiState<T>()
}