package com.movil.paralelo.ui.views

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.movil.paralelo.data.model.TaskItem
import com.movil.paralelo.data.model.TaskStatus
import com.movil.paralelo.ui.theme.*
import com.movil.paralelo.ui.viewmodel.ConcurrencyBenchmarkViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BenchmarkScreen(
    benchmarkViewModel: ConcurrencyBenchmarkViewModel,
    onBack: () -> Unit
) {
    val tasks by benchmarkViewModel.tasks.collectAsState()
    val comparison by benchmarkViewModel.comparison.collectAsState()
    var showTheoryGuide by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Benchmark Concurrencia", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { benchmarkViewModel.resetTasks() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reiniciar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Slate900)
            )
        },
        containerColor = Slate900
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Tarjeta de Resultados Comparativos (Panel Principal)
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Slate800)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        "📊 Comparación Obligatoria (Tiempos)",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 17.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Tiempo Secuencial
                        Column(horizontalAlignment = Alignment.Start) {
                            Text("Versión Secuencial", fontSize = 12.sp, color = Slate400)
                            Text(
                                text = if (comparison.sequentialTime != null)
                                    String.format(Locale.US, "%.2f s", comparison.sequentialTime)
                                else "--",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Amber500
                            )
                        }

                        // Tiempo Concurrente
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Versión Concurrente", fontSize = 12.sp, color = Slate400)
                            Text(
                                text = if (comparison.concurrentTime != null)
                                    String.format(Locale.US, "%.2f s", comparison.concurrentTime)
                                else "--",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Emerald500
                            )
                        }
                    }

                    // Aceleración / Speedup
                    comparison.speedupRatio?.let { speedup ->
                        Divider(color = Slate700, modifier = Modifier.padding(vertical = 12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Aceleración (Speedup):", fontWeight = FontWeight.SemiBold, color = Color.White)
                            Surface(
                                color = Indigo600.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = String.format(Locale.US, "⚡ %.2fx Más Rápido", speedup),
                                    color = Cyan500,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    if (comparison.isRunning) {
                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = if (comparison.currentMode == "CONCURRENTE") Emerald500 else Amber500
                        )
                        Text(
                            "Ejecutando en modo ${comparison.currentMode}...",
                            color = Slate400,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            }

            // Botones de Ejecución
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { benchmarkViewModel.runSequentialBenchmark() },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Amber500),
                    enabled = !comparison.isRunning
                ) {
                    Icon(Icons.Default.Timer, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Secuencial", color = Color.Black, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { benchmarkViewModel.runConcurrentBenchmark() },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                    enabled = !comparison.isRunning
                ) {
                    Icon(Icons.Default.Bolt, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Concurrente", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Lista de Tareas Concurrentes
            Text(
                "Lote de Tareas Procesadas (${tasks.size} archivos independientes)",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Slate400,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            tasks.forEach { task ->
                TaskCard(task = task)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón de Guía para la Exposición en Clase
            OutlinedButton(
                onClick = { showTheoryGuide = !showTheoryGuide },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.School, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (showTheoryGuide) "Ocultar Guía de Exposición" else "Ver Guía de Sustentación para Clase")
            }

            AnimatedVisibility(visible = showTheoryGuide) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate800)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("🎯 Puntos Clave para Explicar al Profesor:", fontWeight = FontWeight.Bold, color = Cyan500)
                        Text("1. Proceso: Subida y procesamiento de 5 archivos independientes en la API.", color = Slate100, fontSize = 13.sp)
                        Text("2. Por qué concurrente: Las peticiones HTTP no tienen dependencia de datos entre sí.", color = Slate100, fontSize = 13.sp)
                        Text("3. Mecanismo en Kotlin: Coroutines con `async(Dispatchers.IO)` y `awaitAll()`.", color = Slate100, fontSize = 13.sp)
                        Text("4. Hilos: Pool elástico administrado por `Dispatchers.IO`.", color = Slate100, fontSize = 13.sp)
                        Text("5. Cuándo NO usar paralelismo: Cuando hay dependencias secuenciales estrictas o el overhead de sincronización supera el tiempo de cómputo.", color = Slate100, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun TaskCard(task: TaskItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Slate800)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val (statusText, statusColor) = when (task.status) {
                TaskStatus.PENDING -> "Pendiente" to Slate400
                TaskStatus.RUNNING -> "En ejecución..." to Cyan500
                TaskStatus.COMPLETED -> "Completado" to Emerald500
                TaskStatus.ERROR -> "Error" to Rose500
            }

            Icon(
                when (task.status) {
                    TaskStatus.COMPLETED -> Icons.Default.CheckCircle
                    TaskStatus.RUNNING -> Icons.Default.Sync
                    TaskStatus.ERROR -> Icons.Default.Error
                    else -> Icons.Default.Schedule
                },
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(task.name, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(
                    text = if (task.timeTakenMs > 0) "Tiempo: ${task.timeTakenMs} ms" else statusText,
                    color = statusColor,
                    fontSize = 12.sp
                )
            }

            Surface(
                color = statusColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = statusText,
                    color = statusColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
