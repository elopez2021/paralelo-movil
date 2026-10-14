package com.movil.paralelo.data.repository

import com.movil.paralelo.data.model.NotificationSendRequest
import com.movil.paralelo.data.model.NotificationSendResponse
import com.movil.paralelo.data.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NotificationRepository(private val apiService: ApiService) {

    suspend fun sendNotification(email: String, subject: String, message: String): Result<NotificationSendResponse> =
        withContext(Dispatchers.IO) {
            try {
                val request = NotificationSendRequest(
                    email = email.trim(),
                    subject = subject.trim(),
                    message = message.trim()
                )
                val response = apiService.sendNotification(request)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    val errorDetail = response.errorBody()?.string() ?: "Codigo ${response.code()}"
                    Result.failure(Exception(errorDetail))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
