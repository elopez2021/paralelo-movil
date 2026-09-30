package com.movil.paralelo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movil.paralelo.data.model.User
import com.movil.paralelo.data.model.UserCreateRequest
import com.movil.paralelo.data.model.UserUpdateRequest
import com.movil.paralelo.data.repository.FileRepository
import com.movil.paralelo.data.repository.UserRepository
import com.movil.paralelo.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UsersViewModel(
    private val userRepository: UserRepository,
    private val fileRepository: FileRepository
) : ViewModel() {

    private val _usersListState = MutableStateFlow<Resource<List<User>>>(Resource.Idle)
    val usersListState: StateFlow<Resource<List<User>>> = _usersListState.asStateFlow()

    private val _userOpState = MutableStateFlow<Resource<String>>(Resource.Idle)
    val userOpState: StateFlow<Resource<String>> = _userOpState.asStateFlow()

    fun loadUsers() {
        viewModelScope.launch {
            _usersListState.value = Resource.Loading
            _usersListState.value = userRepository.getUsers()
        }
    }

    fun createUserWithPhoto(
        nombre: String,
        apellido: String,
        email: String,
        pass: String,
        photoBytes: ByteArray?,
        photoName: String?
    ) {
        viewModelScope.launch {
            _userOpState.value = Resource.Loading
            var photoUrl: String? = null

            // Si el usuario seleccionó una imagen de la galería, se sube primero por multipart
            if (photoBytes != null && photoName != null) {
                val uploadResult = fileRepository.uploadBytes(photoName, photoBytes, "image/jpeg")
                if (uploadResult is Resource.Success) {
                    photoUrl = uploadResult.data.url
                } else if (uploadResult is Resource.Error) {
                    _userOpState.value = Resource.Error("Error al subir imagen: ${uploadResult.message}")
                    return@launch
                }
            }

            val req = UserCreateRequest(
                nombre = nombre.trim(),
                apellido = apellido.trim(),
                email = email.trim(),
                password = pass,
                foto = photoUrl
            )
            val result = userRepository.createUser(req)
            when (result) {
                is Resource.Success -> {
                    _userOpState.value = Resource.Success("Usuario creado correctamente")
                    loadUsers()
                }
                is Resource.Error -> _userOpState.value = Resource.Error(result.message)
                else -> {}
            }
        }
    }

    fun updateUserWithPhoto(
        id: Int,
        nombre: String,
        apellido: String,
        email: String,
        existingFoto: String?,
        newPhotoBytes: ByteArray?,
        newPhotoName: String?
    ) {
        viewModelScope.launch {
            _userOpState.value = Resource.Loading
            var photoUrl: String? = existingFoto

            // Si se seleccionó una nueva foto, subirla a la API
            if (newPhotoBytes != null && newPhotoName != null) {
                val uploadResult = fileRepository.uploadBytes(newPhotoName, newPhotoBytes, "image/jpeg")
                if (uploadResult is Resource.Success) {
                    photoUrl = uploadResult.data.url
                } else if (uploadResult is Resource.Error) {
                    _userOpState.value = Resource.Error("Error al subir nueva imagen: ${uploadResult.message}")
                    return@launch
                }
            }

            val req = UserUpdateRequest(
                nombre = nombre.trim(),
                apellido = apellido.trim(),
                email = email.trim(),
                foto = photoUrl
            )
            val result = userRepository.updateUser(id, req)
            when (result) {
                is Resource.Success -> {
                    _userOpState.value = Resource.Success("Usuario actualizado correctamente")
                    loadUsers()
                }
                is Resource.Error -> _userOpState.value = Resource.Error(result.message)
                else -> {}
            }
        }
    }

    fun deleteUser(id: Int) {
        viewModelScope.launch {
            _userOpState.value = Resource.Loading
            val result = userRepository.deleteUser(id)
            when (result) {
                is Resource.Success -> {
                    _userOpState.value = Resource.Success("Usuario eliminado")
                    loadUsers()
                }
                is Resource.Error -> _userOpState.value = Resource.Error(result.message)
                else -> {}
            }
        }
    }

    fun resetOpState() {
        _userOpState.value = Resource.Idle
    }
}
