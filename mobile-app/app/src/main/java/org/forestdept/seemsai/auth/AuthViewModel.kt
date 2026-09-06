package org.forestdept.seemsai.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _authState = MutableStateFlow(
        AuthState(
            currentUser = repository.getCurrentUser(),
            isAuthenticated = repository.getCurrentUser() != null
        )
    )
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun login(identifier: String, password: String, onSuccess: () -> Unit = {}) {
        _authState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = repository.login(identifier, password)
            result.onSuccess { user ->
                _authState.update {
                    it.copy(
                        currentUser = user,
                        isAuthenticated = true,
                        isLoading = false,
                        errorMessage = null,
                        successMessage = "Welcome, ${user.fullName}!"
                    )
                }
                onSuccess()
            }.onFailure { ex ->
                _authState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = ex.message ?: "Authentication failed."
                    )
                }
            }
        }
    }

    fun loginAsDemo(onSuccess: () -> Unit = {}) {
        _authState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val user = repository.loginAsDemo()
            _authState.update {
                it.copy(
                    currentUser = user,
                    isAuthenticated = true,
                    isLoading = false,
                    errorMessage = null,
                    successMessage = "Logged in as Wildlife Officer"
                )
            }
            onSuccess()
        }
    }

    fun createAccount(
        fullName: String,
        mobileNumber: String,
        email: String,
        password: String,
        confirmPassword: String,
        onSuccess: () -> Unit = {}
    ) {
        _authState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            if (password != confirmPassword) {
                _authState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Passwords do not match."
                    )
                }
                return@launch
            }

            val result = repository.createAccount(
                fullName = fullName,
                mobileNumber = mobileNumber,
                email = email,
                password = password
            )

            result.onSuccess { user ->
                _authState.update {
                    it.copy(
                        currentUser = user,
                        isAuthenticated = true,
                        isLoading = false,
                        errorMessage = null,
                        successMessage = "Account created successfully! Welcome, ${user.fullName}."
                    )
                }
                onSuccess()
            }.onFailure { ex ->
                _authState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = ex.message ?: "Could not create account."
                    )
                }
            }
        }
    }

    fun logout(onLoggedOut: () -> Unit = {}) {
        repository.logout()
        _authState.update {
            AuthState(
                currentUser = null,
                isAuthenticated = false,
                isLoading = false,
                errorMessage = null,
                successMessage = null
            )
        }
        onLoggedOut()
    }

    fun clearMessages() {
        _authState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
