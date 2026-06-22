package com.example.ui

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CivilRegistryViewModel(private val repository: RegistryRepository) : ViewModel() {

    fun registerBirth(childName: String, parentIds: String, hospitalHash: String) {
        viewModelScope.launch {
            val event = CivilRegistry(
                eventType = "BIRTH",
                subjectDetails = "{\"name\": \"$childName\"}",
                parentKinIds = parentIds,
                medicalVerificationHash = hospitalHash,
                status = "REGISTERED"
            )
            repository.insertCivilEvent(event)
            repository.logAction("HOSPITAL-ID", "BIRTH_REGISTRATION", "CivilRegistry", "NEW", "New Birth Registered")
        }
    }

    fun registerDeath(deceasedId: String, medicalHash: String) {
        viewModelScope.launch {
            val event = CivilRegistry(
                eventType = "DEATH",
                subjectDetails = "{\"deceasedId\": \"$deceasedId\"}",
                parentKinIds = "",
                medicalVerificationHash = medicalHash,
                status = "REGISTERED"
            )
            repository.insertCivilEvent(event)
            
            // Rule: Death triggers "Deceased" flag and freezes wallets
            repository.logAction(deceasedId, "DEATH_REGISTRATION", "CivilRegistry", deceasedId, "Profile Frozen: Deceased")
        }
    }

    fun issueCertificate(userId: String, type: String, authorityId: String) {
        viewModelScope.launch {
            val certId = "CERT-${java.util.UUID.randomUUID().toString().take(8)}"
            val cert = IssuedCertificate(
                certificateId = certId,
                userId = userId,
                certificateType = type,
                cryptographicHash = java.util.UUID.randomUUID().toString(),
                issuingAuthorityId = authorityId,
                validUntil = System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000)
            )
            repository.insertCertificate(cert)
            repository.logAction(authorityId, "CERTIFICATE_ISSUANCE", "IssuedCertificate", certId, "Issued $type Certificate")
        }
    }
}
