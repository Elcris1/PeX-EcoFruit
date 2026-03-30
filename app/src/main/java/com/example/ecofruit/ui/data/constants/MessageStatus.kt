package com.example.ecofruit.ui.data.constants

enum class MessageStatus {
    SENDING,    // optimistic, aún no confirmado por el servidor
    SENT,       // entregado al servidor
    DELIVERED,  // entregado al dispositivo del destinatario
    READ,       // leído por el destinatario
    FAILED,     // error de envío
}