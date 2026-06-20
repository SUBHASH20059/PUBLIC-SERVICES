package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "registry_records")
data class RegistryRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // LAND, MARRIAGE, AGREEMENT, LOAN
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

