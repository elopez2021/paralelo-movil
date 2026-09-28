package com.movil.paralelo.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.movil.paralelo.data.model.DashboardCombinedData
import com.movil.paralelo.ui.theme.*
import com.movil.paralelo.ui.viewmodel.DashboardViewModel
import com.movil.paralelo.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    dashboardViewModel: DashboardViewModel,
    onNavigateUsers: () -> Unit,
    onNavigateBenchmark: () -> Unit,
    onLogout: () -> Unit
) {
    val dashboardState by dashboardViewModel.dashboardState.collectAsState()

    LaunchedEffect(Unit) {
        dashboardViewModel.loadDashboardConcurrent()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Dashboard Concurrente", fontWeight = FontWeight.Bold, color = Color.White)
                },
                actions = {
                    IconButton(onClick = { dashboardViewModel.loadDashboardConcurrent() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Recargar", tint = Color.White)
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Salir", tint = Rose500)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Slate900)
            )
        },
        containerColor = Slate900
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = dashboardState) {
                is Resource.Loading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = Indigo500)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Ejecutando consumo concurrente de 4 endpoints...",
                            color = Slate400,
                            fontSize = 14.sp
                        )
                        Text(
                            "users ──┐\nperfil ──┤\nstats ───┤──→ Coroutines\nnotif ───┘",
                            color = Cyan500,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
                is Resource.Success -> {
                    DashboardContent(
                        data = state.data,
                        onNavigateUsers = onNavigateUsers,
                        onNavigateBenchmark = onNavigateBenchmark
                    )
                }
                is Resource.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Error de carga: ${state.message}", color = Rose500)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { dashboardViewModel.loadDashboardConcurrent() }) {
                            Text("Reintentar")
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun DashboardContent(
    data: DashboardCombinedData,
    onNavigateUsers: () -> Unit,
    onNavigateBenchmark: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Banner de Medición del Consumo Simultáneo
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Slate800),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Bolt,
                    contentDescription = null,
                    tint = Emerald500,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        "Consumo Simultáneo Exitoso",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    Text(
                        "4 endpoints resueltos en paralelo con Coroutines en: ${data.executionTimeMs} ms",
                        color = Emerald500,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Saludo Usuario
        Text(
            text = "Bienvenido, ${data.userProfile.nombre} ${data.userProfile.apellido}",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Tarjetas de Métricas (2 columnas)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard(
                title = "Usuarios Registrados",
                value = data.stats.totalUsers.toString(),
                icon = Icons.Default.People,
                accentColor = Indigo500,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Archivos en Servidor",
                value = data.stats.totalFiles.toString(),
                icon = Icons.Default.Folder,
                accentColor = Cyan500,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard(
                title = "Hilos Activos",
                value = data.stats.activeThreads.toString(),
                icon = Icons.Default.Speed,
                accentColor = Amber500,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Estado API",
                value = data.stats.systemStatus,
                icon = Icons.Default.CheckCircle,
                accentColor = Emerald500,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Botones de Acción Rápida
        Text("Módulos Principales", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Slate400)
        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onNavigateBenchmark,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Indigo600),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.CompareArrows, contentDescription = null)
            Spacer(modifier = Modifier.width(10.dp))
            Text("Demostración Concurrencia (Secuencial vs Paralelo)", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onNavigateUsers,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.PersonSearch, contentDescription = null)
            Spacer(modifier = Modifier.width(10.dp))
            Text("Gestión de Usuarios (CRUD)")
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Notificaciones Recientes
        Text("Notificaciones del Sistema", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Slate400)
        Spacer(modifier = Modifier.height(8.dp))

        data.notifications.forEach { notif ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Slate800),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(notif.title, fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 14.sp)
                        Text(notif.timestamp, color = Slate400, fontSize = 11.sp)
                    }
                    Text(notif.message, color = Slate100, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Slate800),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(title, fontSize = 12.sp, color = Slate400)
        }
    }
}
