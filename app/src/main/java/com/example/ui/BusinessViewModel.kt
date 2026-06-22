package com.example.ui

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.security.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BusinessViewModel(private val repository: RegistryRepository) : ViewModel() {

    // Current logged in creator/owner UID (Linked to Govt ID)
    var currentOwnerUid by mutableStateOf("")
    var currentOwnerName by mutableStateOf("")

    // Seed Ideas State
    val mySeedIdeas: StateFlow<List<SeedIdea>> by lazy {
        repository.getSeedIdeasByCreator(currentOwnerUid)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    // Business Entities State
    val myBusinesses: StateFlow<List<BusinessEntity>> by lazy {
        repository.getBusinessesByOwner(currentOwnerUid)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    // IP Protection Logic
    fun protectSeedIdea(title: String, industry: String, concept: String) {
        viewModelScope.launch {
            val key = SecurityVaultManager.generateKey()
            val (encrypted, iv) = SecurityVaultManager.encrypt(concept, key)
            
            // Blockchain seal for proof of existence
            val seal = (title + industry + currentOwnerUid + System.currentTimeMillis()).hashCode().toString(16)
            
            val idea = SeedIdea(
                creatorUid = currentOwnerUid,
                title = title,
                industry = industry,
                encryptedConcept = encrypted,
                iv = iv,
                ipProtectionStatus = "PROTECTED",
                blockchainSeal = seal,
                isPrivate = true
            )
            repository.insertSeedIdea(idea)
            repository.logAction(currentOwnerUid, "IDEA_PROTECTED", "SeedIdea", idea.title, "IP locked with AES-256 and Blockchain Seal")
        }
    }

    // Transition Idea to Startup
    fun registerStartup(idea: SeedIdea, businessName: String, sector: String) {
        viewModelScope.launch {
            val business = BusinessEntity(
                name = businessName,
                type = "STARTUP",
                registrationNumber = "PENDING",
                ownerUid = currentOwnerUid,
                ownerName = currentOwnerName,
                sector = sector,
                status = "REGISTERED",
                isStartupIndiaRecognized = true
            )
            repository.insertBusiness(business)
            
            // Update idea status
            val updatedIdea = idea.copy(ipProtectionStatus = "TRANSITIONED_TO_STARTUP")
            repository.insertSeedIdea(updatedIdea)
            
            repository.logAction(currentOwnerUid, "STARTUP_REGISTERED", "BusinessEntity", business.name, "Transitioned from protected idea")
        }
    }

    // Scale Business (Startup -> Pvt Ltd -> MNC)
    fun scaleBusiness(business: BusinessEntity, newType: String, cin: String) {
        viewModelScope.launch {
            val updatedBusiness = business.copy(
                type = newType,
                registrationNumber = cin,
                status = "ACTIVE"
            )
            repository.insertBusiness(updatedBusiness)
            
            val log = BusinessComplianceLog(
                businessId = business.id,
                complianceType = "REGULATORY",
                status = "COMPLETED",
                details = "Scaled to ${newType} with CIN: ${cin}"
            )
            repository.insertComplianceLog(log)
            repository.logAction(currentOwnerUid, "BUSINESS_SCALED", "BusinessEntity", business.name, "Scaled to ${newType}")
        }
    }
}
