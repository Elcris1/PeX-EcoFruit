package com.example.ecofruit.ui.data.model

data class fcm_token(
    var userId: String = "",
    var token: String = "",
    var active: Boolean = false,
    var createdAt: Long = 0
) {
}