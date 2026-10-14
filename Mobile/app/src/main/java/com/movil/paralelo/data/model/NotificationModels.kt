package com.movil.paralelo.data.model

data class NotificationSendRequest(
    val email: String,
    val subject: String,
    val message: String
)

data class NotificationSendResponse(
    val status: String,
    val message: String,
    val messageId: String? = null
)
