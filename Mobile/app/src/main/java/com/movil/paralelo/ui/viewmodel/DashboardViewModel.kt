package com.movil.paralelo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movil.paralelo.data.model.DashboardCombinedData
import com.movil.paralelo.data.repository.DashboardRepository
import com.movil.paralelo.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.system.measureTimeMillis

class DashboardViewModel(private val dashboardRepository: DashboardRepository) : ViewModel() {

    private val _dashboardState = MutableStateFlow<Resource<DashboardCombinedData>>(Resource.Idle)
    val dashboardState: StateFlow<Resource<DashboardCombinedData>> = _dashboardState.asStateFlow()

    fun loadDashboardConcurrent() {
        viewModelScope.launch {
            _dashboardState.value = Resource.Loading
            try {
                var profileResult: com.movil.paralelo.data.model.User? = null
                var statsResult: com.movil.paralelo.data.model.SystemStats? = null
                var notifResult: List<com.movil.paralelo.data.model.NotificationItem> = emptyList()
                var usersResult: List<com.movil.paralelo.data.model.User> = emptyList()

                // Medimos el tiempo del consumo simultáneo de los 4 endpoints
                val timeTaken = measureTimeMillis {
                    coroutineScope {
                        // Se disparan en paralelo con Coroutines en Dispatchers.IO
                        val profileDeferred = async(Dispatchers.IO) { dashboardRepository.getProfile() }
                        val statsDeferred = async(Dispatchers.IO) { dashboardRepository.getStats() }
                        val notifDeferred = async(Dispatchers.IO) { dashboardRepository.getNotifications() }
                        val usersDeferred = async(Dispatchers.IO) { dashboardRepository.getUsers() }

                        // Esperamos la resolución simultánea
                        profileResult = profileDeferred.await()
                        statsResult = statsDeferred.await()
                        notifResult = notifDeferred.await()
                        usersResult = usersDeferred.await()
                    }
                }

                if (profileResult != null && statsResult != null) {
                    _dashboardState.value = Resource.Success(
                        DashboardCombinedData(
                            userProfile = profileResult,
                            stats = statsResult,
                            notifications = notifResult,
                            usersList = usersResult,
                            executionTimeMs = timeTaken
                        )
                    )
                } else {
                    _dashboardState.value = Resource.Error("Error: Datos incompletos recibidos del servidor")
                }
            } catch (e: Exception) {
                _dashboardState.value = Resource.Error(e.localizedMessage ?: "Error al cargar dashboard")
            }
        }
    }
}
