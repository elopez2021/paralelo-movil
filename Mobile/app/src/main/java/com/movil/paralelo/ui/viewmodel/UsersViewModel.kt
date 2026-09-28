package com.movil.paralelo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movil.paralelo.data.model.User
import com.movil.paralelo.data.model.UserCreateRequest
import com.movil.paralelo.data.model.UserUpdateRequest
import com.movil.paralelo.data.repository.UserRepository
import com.movil.paralelo.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UsersViewModel(private val userRepository: UserRepository) : ViewModel() {

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

    fun createUser(nombre: String, apellido: String, email: String, pass: String, foto: String? = null) {
        viewModelScope.launch {
            _userOpState.value = Resource.Loading
            val req = UserCreateRequest(nombre, apellido, email, pass, foto)
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

    fun updateUser(id: Int, nombre: String, apellido: String, email: String, foto: String? = null) {
        viewModelScope.launch {
            _userOpState.value = Resource.Loading
            val req = UserUpdateRequest(nombre = nombre, apellido = apellido, email = email, foto = foto)
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
