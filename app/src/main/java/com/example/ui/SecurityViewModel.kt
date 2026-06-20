package com.example.ui

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.network.*
import com.example.security.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SecurityViewModel(private val repository: RegistryRepository) : ViewModel() {

    // Employee Auth State
    val currentEmployee = EmployeeAuthManager.currentEmployee
    var employeeIdInput by mutableStateOf("")
    var passwordInput by mutableStateOf("")
    var authError by mutableStateOf("")

    // Audit Trail State
    val auditLogs: StateFlow<List<AuditLog>> = repository.allAuditLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Fraud Detection State
    var fraudCheckResult by mutableStateOf<FraudCheckResponse?>(null)
    var isCheckingFraud by mutableStateOf(false)

    // Secure Vault State
    var vaultItems = mutableStateListOf<SecureVaultRecord>()

    fun login() {
        viewModelScope.launch {
            val success = EmployeeAuthManager.login(employeeIdInput, passwordInput, repository)
            if (!success) {
                authError = "Invalid Employee ID or Password"
            } else {
                authError = ""
                // Load vault items for the employee if needed
            }
        }
    }

    fun performFraudCheck(record: RegistryRecord) {
        viewModelScope.launch {
            isCheckingFraud = true
            try {
                val request = FraudCheckRequest(
                    entityType = "REGISTRY_RECORD",
                    entityData = record.toString()
                )
                fraudCheckResult = NetworkModule.fraudDetectionService.checkForFraud(request)
                repository.logAction(
                    userId = currentEmployee.value?.employeeId ?: "SYSTEM",
                    actionType = "FRAUD_CHECK",
                    entityType = "RegistryRecord",
                    entityId = record.id.toString(),
                    details = "Fraud check completed: ${fraudCheckResult?.riskLevel}"
                )
            } catch (e: Exception) {
                authError = "Fraud check failed: ${e.message}"
            } finally {
                isCheckingFraud = false
            }
        }
    }

    fun addToVault(docType: String, title: String, data: String, ownerUid: String) {
        viewModelScope.launch {
            val key = SecurityVaultManager.generateKey() // In real app, manage keys securely
            val (encrypted, iv) = SecurityVaultManager.encrypt(data, key)
            val record = SecureVaultRecord(
                ownerUid = ownerUid,
                docType = docType,
                docTitle = title,
                encryptedData = encrypted,
                iv = iv
            )
            repository.insertVaultRecord(record)
            repository.logAction(
                userId = currentEmployee.value?.employeeId ?: "SYSTEM",
                actionType = "VAULT_ADD",
                entityType = "SecureVaultRecord",
                entityId = "NEW",
                details = "Added ${docType} to vault for ${ownerUid}"
            )
        }
    }
}
