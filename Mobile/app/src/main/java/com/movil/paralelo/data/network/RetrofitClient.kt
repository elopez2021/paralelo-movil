package com.movil.paralelo.data.network

import android.content.Context
import com.movil.paralelo.utils.Constants
import com.movil.paralelo.utils.SessionManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private var apiService: ApiService? = null

    fun getApiService(context: Context): ApiService {
        if (apiService == null) {
            val sessionManager = SessionManager(context.applicationContext)

            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.HEADERS
            }


            var baseUrl = Constants.BASE_URL.trim()
            if (!baseUrl.startsWith("http://") && !baseUrl.startsWith("https://")) {
                baseUrl = "http://$baseUrl"
            }
            if (!baseUrl.endsWith("/")) {
                baseUrl = "$baseUrl/"
            }
            Constants.BASE_URL = baseUrl

            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(sessionManager))
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            apiService = retrofit.create(ApiService::class.java)
        }
        return apiService!!
    }

    // Permite reconfigurar el cliente si el usuario cambia la IP en caliente
    fun resetClient() {
        apiService = null
    }
}
