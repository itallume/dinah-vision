package com.example.dinahvision.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dinahvision.models.User
import com.example.dinahvision.repository.PrevisionDAO
import com.example.dinahvision.repository.UserDAO
import com.example.dinahvision.ui.state.HomeUiState
import com.example.dinahvision.models.PrevisionFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    private val previsionDAO = PrevisionDAO()
    private val userDAO = UserDAO()

    fun loadPredictions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val predictions = previsionDAO.listPredictionsByUser()
                val points = User.currentUser?.let { userDAO.getUser(it.uid)?.points } ?: 0

                _uiState.value = _uiState.value.copy(
                    predictions = predictions,
                    userPoints = points,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun setFilter(filter: PrevisionFilter) {
        _uiState.value = _uiState.value.copy(currentFilter = filter)
    }

    fun showDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showDialog = show)
    }

    fun updateNewPrevision(prevision: com.example.dinahvision.models.Prevision) {
        _uiState.value = _uiState.value.copy(newPrevision = prevision)
    }

    fun showDatePicker(show: Boolean) {
        _uiState.value = _uiState.value.copy(showDatePicker = show)
    }

    fun selectEndDate(timestamp: Long) {
        _uiState.value = _uiState.value.copy(selectedEndDate = timestamp)
    }

    fun showError(message: String?) {
        _uiState.value = _uiState.value.copy(modalErrorMessage = message, showModalError = message != null)
    }
}