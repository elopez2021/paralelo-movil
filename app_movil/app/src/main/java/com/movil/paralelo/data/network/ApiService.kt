package com.movil.paralelo.data.network

import com.movil.paralelo.data.model.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // 1. Autenticación
    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<TokenResponse>

    @POST("register")
    suspend fun register(@Body request: UserCreateRequest): Response<User>

    // 2. CRUD de Usuarios
    @GET("users")
    suspend fun getUsers(): Response<List<User>>

    @GET("users/{id}")
    suspend fun getUserById(@Path("id") id: Int): Response<User>

    @POST("users")
    suspend fun createUser(@Body request: UserCreateRequest): Response<User>

    @PUT("users/{id}")
    suspend fun updateUser(
        @Path("id") id: Int,
        @Body request: UserUpdateRequest
    ): Response<User>

    @DELETE("users/{id}")
    suspend fun deleteUser(@Path("id") id: Int): Response<Map<String, Any>>

    // 3. Subida de Archivos
    @Multipart
    @POST("upload")
    suspend fun uploadFile(
        @Part file: MultipartBody.Part
    ): Response<FileUploadResponse>

    @DELETE("upload/{id}")
    suspend fun deleteFile(@Path("id") id: Int): Response<Map<String, Any>>

    // 4. Dashboard (Endpoints para consumo simultáneo)
    @GET("dashboard/profile")
    suspend fun getProfile(): Response<User>

    @GET("dashboard/stats")
    suspend fun getStats(): Response<SystemStats>

    @GET("dashboard/notifications")
    suspend fun getNotifications(): Response<List<NotificationItem>>

    // 5. Cálculo Pesado
    @GET("api/proceso/secuencial")
    suspend fun getSequentialComputation(): Response<Map<String, Any>>

    @GET("api/proceso/concurrente")
    suspend fun getConcurrentComputation(): Response<Map<String, Any>>
}
