package com.example.dinahvision.uistate

import com.example.dinahvision.models.Prevision

data class HomeUiState(
    val previsions: List<Prevision> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showAddDialog: Boolean = false,
    val newPrevisionTitle: String = "",
    val newPrevisionDescription: String = "",
    val selectedStartDate: Long? = null,
    val selectedEndDate: Long? = null,
    val showStartDatePicker: Boolean = false,
    val showEndDatePicker: Boolean = false
)
