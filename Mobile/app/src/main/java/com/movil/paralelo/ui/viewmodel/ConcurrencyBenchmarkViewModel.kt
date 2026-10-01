package com.movil.paralelo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movil.paralelo.data.model.ComparisonResult
import com.movil.paralelo.data.model.TaskItem
import com.movil.paralelo.data.model.TaskStatus
import com.movil.paralelo.data.repository.FileRepository
import com.movil.paralelo.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.system.measureTimeMillis

class ConcurrencyBenchmarkViewModel(private val fileRepository: FileRepository) : ViewModel() {

    private val defaultTasks = listOf(
        TaskItem(1, "Archivo_Dataset_1.csv", 1024 * 1024 * 5),  // 5 MB
        TaskItem(2, "Reporte_Metricas_2.pdf", 1024 * 1024 * 8), // 8 MB
        TaskItem(3, "Foto_Perfil_3.jpg", 1024 * 1024 * 4),      // 4 MB
        TaskItem(4, "Datos_Sensores_4.json", 1024 * 1024 * 6),  // 6 MB
        TaskItem(5, "Log_Auditoria_5.txt", 1024 * 1024 * 7)     // 7 MB
    )

    private val _tasks = MutableStateFlow<List<TaskItem>>(defaultTasks.map { it.copy() })
    val tasks: StateFlow<List<TaskItem>> = _tasks.asStateFlow()

    private val _comparison = MutableStateFlow(ComparisonResult())
    val comparison: StateFlow<ComparisonResult> = _comparison.asStateFlow()

    fun resetTasks() {
        _tasks.value = defaultTasks.map { it.copy(status = TaskStatus.PENDING, timeTakenMs = 0L) }
    }

    // --- EJECUCIÓN SECUENCIAL OBLIGATORIA ---
    fun runSequentialBenchmark() {
        viewModelScope.launch {
            resetTasks()
            _comparison.value = _comparison.value.copy(
                isRunning = true,
                currentMode = "SECUENCIAL"
            )

            val currentTaskList = _tasks.value.toMutableList()

            val totalTime = measureTimeMillis {
                for (i in currentTaskList.indices) {
                    val task = currentTaskList[i]

                    // Actualizar estado a RUNNING
                    currentTaskList[i] = task.copy(status = TaskStatus.RUNNING)
                    _tasks.value = currentTaskList.toList()

                    val taskTime = measureTimeMillis {
                        val content = ByteArray(task.sizeBytes.toInt()) { (it % 256).toByte() }
                        val res = fileRepository.uploadBytes(task.name, content, "application/octet-stream")
                        if (res is Resource.Error) {
                            currentTaskList[i] = task.copy(status = TaskStatus.ERROR)
                        } else {
                            currentTaskList[i] = task.copy(status = TaskStatus.COMPLETED)
                        }
                    }

                    currentTaskList[i] = currentTaskList[i].copy(timeTakenMs = taskTime)
                    _tasks.value = currentTaskList.toList()
                }
            }

            val seconds = totalTime / 1000.0
            val speedup = if (_comparison.value.concurrentTime != null && _comparison.value.concurrentTime!! > 0) {
                seconds / _comparison.value.concurrentTime!!
            } else null

            _comparison.value = _comparison.value.copy(
                sequentialTime = seconds,
                speedupRatio = speedup,
                isRunning = false,
                currentMode = null
            )
        }
    }

    // --- 2. EJECUCIÓN CONCURRENTE / PARALELA OBLIGATORIA ---
    fun runConcurrentBenchmark() {
        viewModelScope.launch {
            resetTasks()
            _comparison.value = _comparison.value.copy(
                isRunning = true,
                currentMode = "CONCURRENTE"
            )

            val currentTaskList = _tasks.value.toMutableList()

            // Marcar todas como RUNNING al mismo tiempo
            for (i in currentTaskList.indices) {
                currentTaskList[i] = currentTaskList[i].copy(status = TaskStatus.RUNNING)
            }
            _tasks.value = currentTaskList.toList()

            val totalTime = measureTimeMillis {
                // Se despachan todas simultáneamente en el pool Dispatchers.IO
                val deferredList = currentTaskList.indices.map { index ->
                    async(Dispatchers.IO) {
                        val task = currentTaskList[index]
                        var finalStatus = TaskStatus.COMPLETED
                        val taskTime = measureTimeMillis {
                            val content = ByteArray(task.sizeBytes.toInt()) { (it % 256).toByte() }
                            val res = fileRepository.uploadBytes(task.name, content, "application/octet-stream")
                            if (res is Resource.Error) {
                                finalStatus = TaskStatus.ERROR
                            }
                        }
                        Triple(index, taskTime, finalStatus)
                    }
                }

                // Esperamos la resolución concurrente de todos los hilos
                val results = deferredList.awaitAll()

                for ((idx, timeTaken, finalStatus) in results) {
                    currentTaskList[idx] = currentTaskList[idx].copy(
                        status = finalStatus,
                        timeTakenMs = timeTaken
                    )
                }
                _tasks.value = currentTaskList.toList()
            }

            val seconds = totalTime / 1000.0
            val speedup = if (_comparison.value.sequentialTime != null && seconds > 0) {
                _comparison.value.sequentialTime!! / seconds
            } else null

            _comparison.value = _comparison.value.copy(
                concurrentTime = seconds,
                speedupRatio = speedup,
                isRunning = false,
                currentMode = null
            )
        }
    }
}
