package com.example.security

import com.example.data.Employee
import com.example.data.RegistryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object EmployeeAuthManager {
    private val _currentEmployee = MutableStateFlow<Employee?>(null)
    val currentEmployee: StateFlow<Employee?> = _currentEmployee

    suspend fun login(employeeId: String, passwordHash: String, repository: RegistryRepository): Boolean {
        val employee = repository.getEmployeeById(employeeId)
        return if (employee != null && employee.passwordHash == passwordHash) {
            _currentEmployee.value = employee
            repository.logAction(employeeId, "LOGIN", "Employee", employeeId, "Successful login")
            true
        } else {
            false
        }
    }

    fun logout() {
        _currentEmployee.value = null
    }

    fun isAuthenticated(): Boolean = _currentEmployee.value != null
    
    fun hasRole(role: String): Boolean = _currentEmployee.value?.role == role
}
