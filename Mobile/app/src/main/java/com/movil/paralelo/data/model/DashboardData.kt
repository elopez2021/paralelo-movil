package com.movil.paralelo.data.model

import com.google.gson.annotations.SerializedName

data class SystemStats(
    @SerializedName("total_users") val totalUsers: Int,
    @SerializedName("total_files") val totalFiles: Int,
    @SerializedName("system_status") val systemStatus: String,
    @SerializedName("active_threads") val activeThreads: Int,
    @SerializedName("memory_usage_mb") val memoryUsageMb: Double
)

data class NotificationItem(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("message") val message: String,
    @SerializedName("timestamp") val timestamp: String,
    @SerializedName("type") val type: String
)

data class DashboardCombinedData(
    val userProfile: User,
    val stats: SystemStats,
    val notifications: List<NotificationItem>,
    val usersList: List<User>,
    val executionTimeMs: Long
)
