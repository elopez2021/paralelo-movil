package com.movil.paralelo.data.repository

import com.movil.paralelo.data.model.LoginRequest
import com.movil.paralelo.data.model.TokenResponse
import com.movil.paralelo.data.model.User
import com.movil.paralelo.data.model.UserCreateRequest
import com.movil.paralelo.data.network.ApiService
import com.movil.paralelo.utils.Resource
import com.movil.paralelo.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {
    suspend fun login(email: String, password: String): Resource<TokenResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.login(LoginRequest(email, password))
                if (response.isSuccessful && response.body() != null) {
                    val tokenData = response.body()!!
                    sessionManager.saveAuthToken(tokenData.accessToken)
                    sessionManager.saveUserSession(
                        tokenData.user.id,
                        "${tokenData.user.nombre} ${tokenData.user.apellido}",
                        tokenData.user.email
                    )
                    Resource.Success(tokenData)
                } else {
                    Resource.Error(response.errorBody()?.string() ?: "Error de autenticación")
                }
            } catch (e: Exception) {
                Resource.Error(e.localizedMessage ?: "Error de conexión con el servidor")
            }
        }
    }

    suspend fun register(userRequest: UserCreateRequest): Resource<User> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.register(userRequest)
                if (response.isSuccessful && response.body() != null) {
                    Resource.Success(response.body()!!)
                } else {
                    Resource.Error(response.errorBody()?.string() ?: "Error al registrar usuario")
                }
            } catch (e: Exception) {
                Resource.Error(e.localizedMessage ?: "Error de conexión")
            }
        }
    }

    fun logout() {
        sessionManager.clearSession()
    }

    fun isLoggedIn(): Boolean = sessionManager.isLoggedIn()
}
