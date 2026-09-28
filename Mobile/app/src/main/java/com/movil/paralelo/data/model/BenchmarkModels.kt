package com.movil.paralelo.data.model

data class TaskItem(
    val id: Int,
    val name: String,
    val sizeBytes: Long,
    var status: TaskStatus = TaskStatus.PENDING,
    var timeTakenMs: Long = 0L
)

enum class TaskStatus {
    PENDING,
    RUNNING,
    COMPLETED,
    ERROR
}

data class BenchmarkSummary(
    val mode: String, // "SECUENCIAL" o "CONCURRENTE"
    val totalTasks: Int,
    val totalTimeSeconds: Double,
    val tasks: List<TaskItem>
)

data class ComparisonResult(
    val sequentialTime: Double? = null,
    val concurrentTime: Double? = null,
    val speedupRatio: Double? = null,
    val isRunning: Boolean = false,
    val currentMode: String? = null
)
