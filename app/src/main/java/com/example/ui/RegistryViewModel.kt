package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RegistryViewModel(private val repository: RegistryRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<String>("")
    val uiState: StateFlow<String> = _uiState

    fun performEmployeeAction(employee: Employee, action: () -> Unit) {
        if (repository.isWorkingDay()) {
            action()
            _uiState.value = "Action performed successfully."
        } else {
            _uiState.value = "Modifications can only be done on working days (Mon-Fri)."
        }
    }

    // Citizens can apply anytime
    fun citizenApplication(citizenId: String, details: String) {
        viewModelScope.launch {
            // Application logic here
            _uiState.value = "Application submitted successfully. Citizens can apply 24/7."
        }
    }
}
