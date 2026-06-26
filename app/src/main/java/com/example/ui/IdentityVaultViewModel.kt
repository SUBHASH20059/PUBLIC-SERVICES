package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class IdentityVaultViewModel(private val repository: RegistryRepository) : ViewModel() {

    fun getIdentityDocuments(userId: String): Flow<List<IdentityDocument>> = 
        repository.getIdentityDocuments(userId)

    fun uploadDocument(userId: String, type: String, number: String, authority: String, data: String) {
        viewModelScope.launch {
            val doc = IdentityDocument(
                userId = userId,
                documentType = type,
                documentNumber = number,
                issuingAuthority = authority,
                encryptedData = data, // In real app, encrypt before storage
                iv = "fixed-iv-for-demo",
                status = "PENDING",
                issueDate = System.currentTimeMillis(),
                expiryDate = null
            )
            repository.insertIdentityDocument(doc)
            repository.logAction(userId, "DOCUMENT_UPLOAD", "IdentityDocument", number, "Uploaded $type")
        }
    }

    fun verifyDocument(officerId: String, docId: Int, docNumber: String) {
        viewModelScope.launch {
            // Logic to update status would go here
            repository.logAction(officerId, "DOCUMENT_VERIFICATION", "IdentityDocument", docNumber, "Verified by Officer")
        }
    }
}
