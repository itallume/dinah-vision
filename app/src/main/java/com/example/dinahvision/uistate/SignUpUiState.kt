package com.example.dinahvision.uistate

data class SignUpUiState(
    val username: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val isRegistrationSuccessful: Boolean = false,
    val errorMessage: String? = null
)
