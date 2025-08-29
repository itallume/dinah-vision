package com.example.dinahvision.ui.state

import com.example.dinahvision.models.Prevision
import com.example.dinahvision.models.PrevisionFilter

data class HomeUiState(
    val isLoading: Boolean = true,
    val predictions: List<Prevision> = emptyList(),
    val userPoints: Int = 0,
    val showDialog: Boolean = false,
    val newPrevision: Prevision = Prevision(),
    val showDatePicker: Boolean = false,
    val selectedEndDate: Long? = null, // Timestamp em millis
    val modalErrorMessage: String? = null,
    val showModalError: Boolean = false,
    val currentFilter: PrevisionFilter = PrevisionFilter.CURRENT
)