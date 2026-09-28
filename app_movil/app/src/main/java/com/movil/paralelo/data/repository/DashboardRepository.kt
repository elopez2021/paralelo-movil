package com.movil.paralelo.data.repository

import com.movil.paralelo.data.model.*
import com.movil.paralelo.data.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DashboardRepository(private val apiService: ApiService) {

    suspend fun getProfile(): User = withContext(Dispatchers.IO) {
        val resp = apiService.getProfile()
        if (resp.isSuccessful && resp.body() != null) resp.body()!!
        else throw Exception("Error al cargar perfil")
    }

    suspend fun getStats(): SystemStats = withContext(Dispatchers.IO) {
        val resp = apiService.getStats()
        if (resp.isSuccessful && resp.body() != null) resp.body()!!
        else throw Exception("Error al cargar estadísticas")
    }

    suspend fun getNotifications(): List<NotificationItem> = withContext(Dispatchers.IO) {
        val resp = apiService.getNotifications()
        if (resp.isSuccessful && resp.body() != null) resp.body()!!
        else emptyList()
    }

    suspend fun getUsers(): List<User> = withContext(Dispatchers.IO) {
        val resp = apiService.getUsers()
        if (resp.isSuccessful && resp.body() != null) resp.body()!!
        else emptyList()
    }
}
