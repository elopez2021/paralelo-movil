package com.movil.paralelo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movil.paralelo.data.model.TokenResponse
import com.movil.paralelo.data.model.User
import com.movil.paralelo.data.model.UserCreateRequest
import com.movil.paralelo.data.repository.AuthRepository
import com.movil.paralelo.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _loginState = MutableStateFlow<Resource<TokenResponse>>(Resource.Idle)
    val loginState: StateFlow<Resource<TokenResponse>> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow<Resource<User>>(Resource.Idle)
    val registerState: StateFlow<Resource<User>> = _registerState.asStateFlow()

    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _loginState.value = Resource.Error("Por favor completa todos los campos")
            return
        }
        viewModelScope.launch {
            _loginState.value = Resource.Loading
            _loginState.value = authRepository.login(email.trim(), pass)
        }
    }

    fun register(nombre: String, apellido: String, email: String, pass: String) {
        if (nombre.isBlank() || apellido.isBlank() || email.isBlank() || pass.isBlank()) {
            _registerState.value = Resource.Error("Por favor completa todos los campos")
            return
        }
        viewModelScope.launch {
            _registerState.value = Resource.Loading
            val req = UserCreateRequest(nombre.trim(), apellido.trim(), email.trim(), pass)
            _registerState.value = authRepository.register(req)
        }
    }

    fun logout() {
        authRepository.logout()
        _loginState.value = Resource.Idle
    }

    fun isLoggedIn(): Boolean = authRepository.isLoggedIn()
}
