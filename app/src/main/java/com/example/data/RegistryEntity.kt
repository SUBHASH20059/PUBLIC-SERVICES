package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "registry_records")
data class RegistryRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // LAND, MARRIAGE, AGREEMENT, LOAN, BUSINESS
    val title: String,
    val ownerName: String,
    val ownerUniqueId: String, // Government-issued unique ID (e.g. Aadhaar / PAN)
    val description: String,
    val additionalParties: String, // Comma-separated list of other parties
    val status: String, // PENDING, APPROVED, REJECTED
    val constitutionStatutes: String, // Relevant law of India (e.g., Transfer of Property Act Sec 54)
    val iasClearance: Boolean = false, // IAS clearing for verification
    val incomeTaxClearance: Boolean = false, // Income Tax Clearance for verification
    val createdTimestamp: Long = System.currentTimeMillis(),
    val courtOrderLinked: String? = null, // Linked Court Order for changes
    val chargeAmount: Double = 0.0, // Any financial liability or charged amount on registry (can only be changed via Court Order)
    val verifiedByOfficer: String? = null,
    val signatureStatus: String = "NOT_SCANNED", // NOT_SCANNED, VERIFIED_AUTHENTIC, FORGED_FLAGGED
    val signatureMatchRate: Int = 0, // 0 to 100 percentage
    val duplicateAttemptFound: Boolean = false,
    val scanLog: String = "" // Summary of fraud analysis results
)

@Entity(tableName = "ownership_change_requests")
data class OwnershipChangeRequest(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val recordId: Int, // Ref to original RegistryRecord.id
    val recordTitle: String,
    val currentOwnerName: String,
    val currentOwnerUniqueId: String,
    val requestedNewOwnerName: String,
    val requestedNewOwnerUniqueId: String,
    val courtOrderNumber: String, // Required by prompt: "can't be charged in any form other than court orders" and authorized requests need Court Orders
    val reason: String,
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED
    val requestedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "court_orders")
data class CourtOrder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val orderNumber: String, // Unique Indian Court Reference Number (e.g., CIVIL/DL/2026/8942)
    val courtName: String, // e.g., Supreme Court of India, Delhi High Court
    val details: String, // Decree or mandate detail (e.g., "Direct transfer of land survey 102 to Asha Kelkar")
    val recordId: Int, // Target record ID
    val mandatedNewOwnerName: String?,
    val mandatedNewOwnerUniqueId: String?,
    val mandatedCharge: Double?, // Court ordered charge/liability
    val issuedDate: String = "2026-06-19",
    val isExecuted: Boolean = false
)

@Entity(tableName = "blockchain_ledger")
data class BlockchainBlock(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val blockIndex: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val previousHash: String,
    val hash: String,
    val recordId: Int, // Associated property/registry record
    val transactionType: String, // E.g., GENESIS, DEED_GEN, TITLE_TRANS, CHARGE_MOD, LIEN_ADDED
    val payload: String, // Immutable JSON-like detail block
    val nonce: Long
)

@Entity(tableName = "property_valuations")
data class PropertyValuation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val propertyName: String,
    val surveyorName: String,
    val zoneClassification: String, // Premium Commercial, High-Density Urban, Semi-Urban, Agricultural
    val regionalGuidelineRate: Double, // Gov guideline value per sq ft
    val landAreaSqFt: Double,
    val developmentalPremiumMultiplier: Double, // 1.0 to 2.5
    val overallAssessedValue: Double,
    val blockchainSealHash: String,
    val createdTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "employees")
data class Employee(
    @PrimaryKey val employeeId: String,
    val fullName: String,
    val role: String, // OFFICER, AUDITOR, ADMIN
    val department: String,
    val email: String,
    val passwordHash: String,
    val lastLogin: Long = 0L
)

@Entity(tableName = "audit_logs")
data class AuditLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val userId: String, // Employee ID or Citizen UID
    val actionType: String, // LOGIN, CREATE_RECORD, UPDATE_RECORD, VAULT_ACCESS
    val entityType: String, // RegistryRecord, CourtOrder, etc.
    val entityId: String,
    val details: String,
    val ipAddress: String,
    val deviceId: String,
    val checksum: String // Cryptographic seal for the log entry
)

@Entity(tableName = "secure_vault_records")
data class SecureVaultRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val ownerUid: String,
    val docType: String,
    val docTitle: String,
    val encryptedData: String, // AES-256 Encrypted
    val iv: String, // Initialization Vector for AES
    val createdTimestamp: Long = System.currentTimeMillis()
)

// --- BUSINESS & STARTUP MODULE ---

@Entity(tableName = "business_entities")
data class BusinessEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: String, // SEED_IDEA, STARTUP, PRIVATE_LIMITED, PUBLIC_LIMITED, MNC
    val registrationNumber: String?, // CIN for companies
    val ownerUid: String, // Linked to Government ID (Aadhaar/PAN)
    val ownerName: String,
    val sector: String,
    val status: String, // REGISTERED, ACTIVE, COMPLIANCE_PENDING, DORMANT
    val incorporationDate: Long = System.currentTimeMillis(),
    val isStartupIndiaRecognized: Boolean = false,
    val complianceScore: Int = 100
)

@Entity(tableName = "seed_ideas")
data class SeedIdea(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val creatorUid: String, // Linked to Government ID
    val title: String,
    val industry: String,
    val encryptedConcept: String, // AES-256 Protected
    val iv: String,
    val ipProtectionStatus: String, // PENDING, PROTECTED, PUBLIC
    val blockchainSeal: String, // Proof of existence hash
    val createdTimestamp: Long = System.currentTimeMillis(),
    val isPrivate: Boolean = true // Full privacy for creator
)

@Entity(tableName = "business_compliance_logs")
data class BusinessComplianceLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val businessId: Int,
    val complianceType: String, // TAX, REGULATORY, LABOR, ENVIRONMENTAL
    val status: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)
