package com.movil.paralelo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movil.paralelo.data.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NotificationUiState(
    val email: String = "",
    val subject: String = "",
    val message: String = "",
    val isLoading: Boolean = false,
    val feedbackMessage: String? = null,
    val isSuccess: Boolean? = null
)

class NotificationViewModel(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    fun onEmailChanged(newEmail: String) {
        _uiState.value = _uiState.value.copy(email = newEmail, feedbackMessage = null)
    }

    fun onSubjectChanged(newSubject: String) {
        _uiState.value = _uiState.value.copy(subject = newSubject, feedbackMessage = null)
    }

    fun onMessageChanged(newMessage: String) {
        _uiState.value = _uiState.value.copy(message = newMessage, feedbackMessage = null)
    }

    fun sendNotification() {
        val currentState = _uiState.value
        val email = currentState.email.trim()
        val subject = currentState.subject.trim()
        val message = currentState.message.trim()

        if (email.isEmpty() || subject.isEmpty() || message.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                feedbackMessage = "Error al enviar mensaje.",
                isSuccess = false
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, feedbackMessage = null)

            val result = notificationRepository.sendNotification(email, subject, message)
            result.fold(
                onSuccess = {
                    _uiState.value = NotificationUiState(
                        email = "",
                        subject = "",
                        message = "",
                        isLoading = false,
                        feedbackMessage = "Mensaje enviado correctamente.",
                        isSuccess = true
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        feedbackMessage = "Error al enviar mensaje.",
                        isSuccess = false
                    )
                }
            )
        }
    }

    fun clearFeedback() {
        _uiState.value = _uiState.value.copy(feedbackMessage = null, isSuccess = null)
    }
}
