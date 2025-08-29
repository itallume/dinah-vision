package com.example.dinahvision.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dinahvision.models.Prevision
import com.example.dinahvision.models.User
import com.example.dinahvision.repository.PrevisionDAO
import com.example.dinahvision.uistate.HomeUiState
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

class HomeViewModel(
    private val previsionDAO: PrevisionDAO = PrevisionDAO()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadPrevisions()
    }

    fun loadPrevisions() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        
        viewModelScope.launch {
            try {
                val currentUser = User.currentUser
                if (currentUser != null) {
                    val previsions = previsionDAO.getPrevisionsByUser(currentUser.uid)
                    _uiState.value = _uiState.value.copy(
                        previsions = previsions,
                        isLoading = false,
                        errorMessage = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Usuário não logado"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Erro ao carregar previsões: ${e.message}"
                )
            }
        }
    }

    fun showAddDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = true)
    }

    fun hideAddDialog() {
        _uiState.value = _uiState.value.copy(
            showAddDialog = false,
            newPrevisionTitle = "",
            newPrevisionDescription = "",
            selectedStartDate = null,
            selectedEndDate = null
        )
    }

    fun updateNewPrevisionTitle(title: String) {
        _uiState.value = _uiState.value.copy(newPrevisionTitle = title)
    }

    fun updateNewPrevisionDescription(description: String) {
        _uiState.value = _uiState.value.copy(newPrevisionDescription = description)
    }

    fun showStartDatePicker() {
        _uiState.value = _uiState.value.copy(showStartDatePicker = true)
    }

    fun hideStartDatePicker() {
        _uiState.value = _uiState.value.copy(showStartDatePicker = false)
    }

    fun showEndDatePicker() {
        _uiState.value = _uiState.value.copy(showEndDatePicker = true)
    }

    fun hideEndDatePicker() {
        _uiState.value = _uiState.value.copy(showEndDatePicker = false)
    }

    fun updateStartDate(date: Long) {
        _uiState.value = _uiState.value.copy(
            selectedStartDate = date,
            showStartDatePicker = false
        )
    }

    fun updateEndDate(date: Long) {
        _uiState.value = _uiState.value.copy(
            selectedEndDate = date,
            showEndDatePicker = false
        )
    }

    fun addPrevision() {
        val currentState = _uiState.value
        val currentUser = User.currentUser

        if (currentUser == null) {
            _uiState.value = currentState.copy(errorMessage = "Usuário não logado")
            return
        }

        if (currentState.newPrevisionTitle.isBlank()) {
            _uiState.value = currentState.copy(errorMessage = "Título é obrigatório")
            return
        }

        if (currentState.selectedStartDate == null || currentState.selectedEndDate == null) {
            _uiState.value = currentState.copy(errorMessage = "Selecione as datas")
            return
        }

        _uiState.value = currentState.copy(isLoading = true)

        viewModelScope.launch {
            try {
                val newPrevision = Prevision(
                    title = currentState.newPrevisionTitle,
                    description = currentState.newPrevisionDescription,
                    startDate = Timestamp(Date(currentState.selectedStartDate!!)),
                    endDate = Timestamp(Date(currentState.selectedEndDate!!)),
                    userId = currentUser.uid,
                    predicted = false,
                    finished = false
                )

                previsionDAO.addPrevision(newPrevision)
                
                // Recarregar as previsões
                loadPrevisions()
                
                // Fechar o diálogo
                hideAddDialog()
                
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    isLoading = false,
                    errorMessage = "Erro ao adicionar previsão: ${e.message}"
                )
            }
        }
    }

    fun markPrevisionAsCorrect(previsionId: String) {
        viewModelScope.launch {
            try {
                val success = previsionDAO.markPredictionAsCorrect(previsionId)
                if (success) {
                    loadPrevisions() // Recarregar lista
                } else {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Erro ao marcar previsão como correta"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Erro: ${e.message}"
                )
            }
        }
    }

    fun markPrevisionAsIncorrect(previsionId: String) {
        viewModelScope.launch {
            try {
                val success = previsionDAO.markPredictionAsIncorrect(previsionId)
                if (success) {
                    loadPrevisions() // Recarregar lista
                } else {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Erro ao marcar previsão como incorreta"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Erro: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
