package com.movil.paralelo.data.repository

import com.movil.paralelo.data.model.User
import com.movil.paralelo.data.model.UserCreateRequest
import com.movil.paralelo.data.model.UserUpdateRequest
import com.movil.paralelo.data.network.ApiService
import com.movil.paralelo.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(private val apiService: ApiService) {

    suspend fun getUsers(): Resource<List<User>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getUsers()
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Error al obtener usuarios")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error de red")
        }
    }

    suspend fun getUserById(id: Int): Resource<User> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getUserById(id)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Usuario no encontrado")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error de red")
        }
    }

    suspend fun createUser(request: UserCreateRequest): Resource<User> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.createUser(request)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Error al crear usuario")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error de red")
        }
    }

    suspend fun updateUser(id: Int, request: UserUpdateRequest): Resource<User> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.updateUser(id, request)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Error al actualizar usuario")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error de red")
        }
    }

    suspend fun deleteUser(id: Int): Resource<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteUser(id)
            if (response.isSuccessful) {
                Resource.Success(true)
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Error al eliminar usuario")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error de red")
        }
    }
}
