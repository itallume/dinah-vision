package com.example.dinahvision.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dinahvision.repository.UserDAO
import com.example.dinahvision.uistate.SignUpUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SignUpViewModel(
    private val userDAO: UserDAO = UserDAO()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    fun updateUsername(username: String) {
        _uiState.value = _uiState.value.copy(username = username)
    }

    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }

    fun updateConfirmPassword(confirmPassword: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = confirmPassword)
    }

    fun signUp() {
        val currentState = _uiState.value
        
        // Validações
        if (currentState.username.isBlank() || 
            currentState.password.isBlank() || 
            currentState.confirmPassword.isBlank()) {
            _uiState.value = currentState.copy(
                errorMessage = "Por favor, preencha todos os campos"
            )
            return
        }

        if (currentState.password != currentState.confirmPassword) {
            _uiState.value = currentState.copy(
                errorMessage = "As senhas não coincidem"
            )
            return
        }

        if (currentState.password.length < 6) {
            _uiState.value = currentState.copy(
                errorMessage = "A senha deve ter pelo menos 6 caracteres"
            )
            return
        }

        _uiState.value = currentState.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                val success = userDAO.register(currentState.username, currentState.password)
                if (success) {
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        isRegistrationSuccessful = true
                    )
                } else {
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        errorMessage = "Usuário já existe ou erro no registro"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    isLoading = false,
                    errorMessage = "Erro ao registrar: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
