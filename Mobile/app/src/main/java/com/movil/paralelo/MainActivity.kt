package com.movil.paralelo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.movil.paralelo.data.network.RetrofitClient
import com.movil.paralelo.data.repository.AuthRepository
import com.movil.paralelo.data.repository.DashboardRepository
import com.movil.paralelo.data.repository.FileRepository
import com.movil.paralelo.data.repository.UserRepository
import com.movil.paralelo.ui.theme.ParaleloMovilTheme
import com.movil.paralelo.ui.theme.Slate900
import com.movil.paralelo.ui.viewmodel.AuthViewModel
import com.movil.paralelo.ui.viewmodel.ConcurrencyBenchmarkViewModel
import com.movil.paralelo.ui.viewmodel.DashboardViewModel
import com.movil.paralelo.ui.viewmodel.UsersViewModel
import com.movil.paralelo.ui.views.BenchmarkScreen
import com.movil.paralelo.ui.views.DashboardScreen
import com.movil.paralelo.ui.views.LoginScreen
import com.movil.paralelo.ui.views.UsersScreen
import com.movil.paralelo.utils.SessionManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ParaleloMovilTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Slate900
                ) {
                    AppNavigation(activity = this)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(activity: ComponentActivity) {
    val navController = rememberNavController()
    val context = activity.applicationContext

    // Inicialización de componentes (Capa MVVM)
    val sessionManager = remember { SessionManager(context) }
    val apiService = remember { RetrofitClient.getApiService(context) }

    val authRepository = remember { AuthRepository(apiService, sessionManager) }
    val userRepository = remember { UserRepository(apiService) }
    val fileRepository = remember { FileRepository(apiService) }
    val dashboardRepository = remember { DashboardRepository(apiService) }

    val authViewModel = remember { AuthViewModel(authRepository) }
    val usersViewModel = remember { UsersViewModel(userRepository, fileRepository) }
    val dashboardViewModel = remember { DashboardViewModel(dashboardRepository) }
    val benchmarkViewModel = remember { ConcurrencyBenchmarkViewModel(fileRepository) }

    val startDestination = if (sessionManager.isLoggedIn()) "dashboard" else "login"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("dashboard") {
            DashboardScreen(
                dashboardViewModel = dashboardViewModel,
                onNavigateUsers = { navController.navigate("users") },
                onNavigateBenchmark = { navController.navigate("benchmark") },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate("login") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                }
            )
        }

        composable("users") {
            UsersScreen(
                usersViewModel = usersViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("benchmark") {
            BenchmarkScreen(
                benchmarkViewModel = benchmarkViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
