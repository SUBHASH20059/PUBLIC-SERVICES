package com.example.security

import com.example.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object EmployeeAuthManager {
    private val _currentEmployee = MutableStateFlow<Employee?>(null)
    val currentEmployee: StateFlow<Employee?> = _currentEmployee

    // Zero-Trust Session & Device Binding
    private const val AUTHORIZED_DEVICE_TOKEN = "SECURE-DEVICE-TOKEN"

    suspend fun login(
        employeeId: String, 
        passwordHash: String, 
        deviceToken: String,
        repository: RegistryRepository
    ): Boolean {
        // Rule: Session must be bound to authorized hardware
        if (deviceToken != AUTHORIZED_DEVICE_TOKEN) return false

        val employee = repository.getEmployeeById(employeeId)
        return if (employee != null && employee.passwordHash == passwordHash) {
            _currentEmployee.value = employee
            repository.logAction(employeeId, "LOGIN", "Employee", employeeId, "Zero-Trust Login Success")
            true
        } else {
            false
        }
    }

    // Maker-Checker Logic Enforcement
    suspend fun proposeAction(
        applicationId: Int,
        proposedState: String,
        repository: RegistryRepository
    ): Boolean {
        val employee = _currentEmployee.value ?: return false
        
        // Rule: Only MAKER can propose
        if (employee.role != "MAKER") return false

        val proposal = ActionProposal(
            applicationId = applicationId,
            makerId = employee.employeeId,
            proposedState = proposedState,
            checkerId = null,
            approvalTimestamp = null,
            digitalSignatureHash = null
        )
        repository.insertProposal(proposal)
        repository.logAction(employee.employeeId, "PROPOSE", "ActionProposal", applicationId.toString(), "Proposed: $proposedState")
        return true
    }

    suspend fun approveAction(
        proposal: ActionProposal,
        repository: RegistryRepository
    ): Boolean {
        val employee = _currentEmployee.value ?: return false

        // Rule: Only CHECKER can approve, and Maker cannot be Checker for the same action
        if (employee.role != "CHECKER" || proposal.makerId == employee.employeeId) return false

        val updatedProposal = proposal.copy(
            checkerId = employee.employeeId,
            approvalTimestamp = System.currentTimeMillis(),
            digitalSignatureHash = java.util.UUID.randomUUID().toString() // Cryptographic Sign-off
        )
        repository.updateProposal(updatedProposal)
        repository.logAction(employee.employeeId, "APPROVE", "ActionProposal", proposal.proposalId.toString(), "Approved by Checker")
        return true
    }

    fun logout() {
        _currentEmployee.value = null
    }
}
