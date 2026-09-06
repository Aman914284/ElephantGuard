package org.forestdept.seemsai.auth

/**
 * Represents an authenticated user profile in the local Elephant Guard prototype.
 */
data class UserAccount(
    val fullName: String,
    val mobileNumber: String,
    val email: String,
    val password: String,
    val role: String = "Forest Patrol & Wildlife Officer",
    val isDemo: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis()
)

/**
 * Screen state representation for authentication and current active session.
 */
data class AuthState(
    val currentUser: UserAccount? = null,
    val isAuthenticated: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

/**
 * Generic field validation result.
 */
data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
)
