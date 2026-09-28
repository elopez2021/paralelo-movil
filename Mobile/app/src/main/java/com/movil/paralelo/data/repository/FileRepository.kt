package com.movil.paralelo.data.repository

import com.movil.paralelo.data.model.FileUploadResponse
import com.movil.paralelo.data.network.ApiService
import com.movil.paralelo.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class FileRepository(private val apiService: ApiService) {

    suspend fun uploadFile(file: File): Resource<FileUploadResponse> = withContext(Dispatchers.IO) {
        try {
            val requestFile = file.asRequestBody("application/octet-stream".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
            val response = apiService.uploadFile(body)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Error al subir archivo")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error de red al subir archivo")
        }
    }

    suspend fun uploadBytes(filename: String, bytes: ByteArray, mimeType: String = "text/plain"): Resource<FileUploadResponse> = withContext(Dispatchers.IO) {
        try {
            val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", filename, requestBody)
            val response = apiService.uploadFile(body)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Error al subir archivo")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error de red al subir archivo")
        }
    }

    suspend fun deleteFile(id: Int): Resource<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteFile(id)
            if (response.isSuccessful) {
                Resource.Success(true)
            } else {
                Resource.Error("No se pudo eliminar el archivo")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error de red")
        }
    }
}
