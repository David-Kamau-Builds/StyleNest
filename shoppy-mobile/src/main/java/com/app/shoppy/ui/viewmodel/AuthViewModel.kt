package com.app.shoppy.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.shoppy.data.local.SessionManager
import com.app.shoppy.data.remote.ShoppyApiService
import com.app.shoppy.data.remote.model.UserDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: UserDto) : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val apiService: ShoppyApiService,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun login(email: String, pass: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val response = apiService.login(email, pass)
                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!
                    sessionManager.saveUserSession(user.email, user.name, user.avatarUrl, user.twoFactorEnabled)
                    _authState.value = AuthState.Success(user)
                } else {
                    _authState.value = AuthState.Error("Invalid credentials or user not found.")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Network Error: ${e.message}")
            }
        }
    }

    fun register(name: String, email: String, pass: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val newUser = UserDto(
                    id = 0,
                    name = name,
                    email = email,
                    avatarUrl = null,
                    twoFactorEnabled = false,
                    loyaltyPoints = 0,
                    password = pass
                )
                val response = apiService.register(newUser)
                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!
                    sessionManager.saveUserSession(user.email, user.name, user.avatarUrl, user.twoFactorEnabled)
                    _authState.value = AuthState.Success(user)
                } else {
                    _authState.value = AuthState.Error("Registration failed.")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Network Error: ${e.message}")
            }
        }
    }

    fun fetchUserProfile(email: String) {
        viewModelScope.launch {
            try {
                val response = apiService.getUserProfile(email)
                if (response.isSuccessful && response.body() != null) {
                    _authState.value = AuthState.Success(response.body()!!)
                }
            } catch (e: Exception) {
                // Silently fail or log error for profile fetch
            }
        }
    }

    fun updateProfile(email: String, updatedUser: UserDto) {
        viewModelScope.launch {
            try {
                val response = apiService.updateUserProfile(email, updatedUser)
                if (response.isSuccessful && response.body() != null) {
                    _authState.value = AuthState.Success(response.body()!!)
                }
            } catch (e: Exception) {
                // Silently fail or log error
            }
        }
    }
}
