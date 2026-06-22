package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "registry_records")
data class RegistryRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // LAND, MARRIAGE, AGREEMENT, LOAN, BUSINESS
    val title: String,
    val ownerName: String,
    val ownerUniqueId: String,
    val description: String,
    val additionalParties: String,
    val status: String,
    val constitutionStatutes: String,
    val iasClearance: Boolean = false,
    val incomeTaxClearance: Boolean = false,
    val createdTimestamp: Long = System.currentTimeMillis(),
    val courtOrderLinked: String? = null,
    val chargeAmount: Double = 0.0,
    val verifiedByOfficer: String? = null,
    val signatureStatus: String = "NOT_SCANNED",
    val signatureMatchRate: Int = 0,
    val duplicateAttemptFound: Boolean = false,
    val scanLog: String = ""
)

// --- GST & TAXATION MODULE ---

@Entity(tableName = "gst_profiles")
data class GstProfile(
    @PrimaryKey val gstin: String,
    val businessId: Int,
    val stateCode: String,
    val registrationDate: Long,
    val status: String // ACTIVE, SUSPENDED, CANCELLED
)

@Entity(tableName = "invoice_ledger")
data class InvoiceLedger(
    @PrimaryKey(autoGenerate = true) val invoiceId: Int = 0,
    val supplierGstin: String,
    val recipientGstin: String,
    val taxableValue: Double,
    val gstAmount: Double,
    val itcEligible: Boolean,
    val isMatchedFlag: Boolean = false
)

// --- ZERO-TRUST EMPLOYEE SYSTEM ---

@Entity(tableName = "employees")
data class Employee(
    @PrimaryKey val employeeId: String,
    val fullName: String,
    val hierarchyLevel: Int, // 1 to 5
    val role: String, // MAKER, CHECKER, AUDITOR
    val assignedDepartment: String,
    val slaRating: Double = 5.0,
    val activeDeviceToken: String?,
    val email: String,
    val passwordHash: String,
    val lastLogin: Long = 0L
)

@Entity(tableName = "action_proposals")
data class ActionProposal(
    @PrimaryKey(autoGenerate = true) val proposalId: Int = 0,
    val applicationId: Int,
    val makerId: String,
    val proposedState: String,
    val checkerId: String?,
    val approvalTimestamp: Long?,
    val digitalSignatureHash: String?
)

// --- CIVIL REGISTRIES & CERTIFICATES ---

@Entity(tableName = "civil_registries")
data class CivilRegistry(
    @PrimaryKey(autoGenerate = true) val eventId: Int = 0,
    val eventType: String, // BIRTH, DEATH
    val subjectDetails: String, // JSON blob
    val parentKinIds: String,
    val medicalVerificationHash: String,
    val status: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "issued_certificates")
data class IssuedCertificate(
    @PrimaryKey val certificateId: String,
    val userId: String,
    val certificateType: String, // INCOME, CASTE, RESIDENCE
    val cryptographicHash: String,
    val issuingAuthorityId: String,
    val validUntil: Long
)

// --- WELFARE SCHEMES ---

@Entity(tableName = "schemes_applications")
data class SchemeApplication(
    @PrimaryKey(autoGenerate = true) val applicationId: Int = 0,
    val schemeId: Int,
    val criteriaJson: String,
    val citizenId: String,
    val currentStage: String,
    val paymentStatus: String // PENDING, DISBURSED
)

// --- AUDIT LOGS (IMMUTABLE) ---

@Entity(tableName = "audit_logs")
data class AuditLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val userId: String,
    val actionType: String,
    val entityType: String,
    val entityId: String,
    val details: String,
    val ipAddress: String,
    val deviceId: String,
    val checksum: String
)

// --- PREVIOUS MODULES (RETAINED) ---

@Entity(tableName = "ownership_change_requests")
data class OwnershipChangeRequest(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val recordId: Int,
    val recordTitle: String,
    val currentOwnerName: String,
    val currentOwnerUniqueId: String,
    val requestedNewOwnerName: String,
    val requestedNewOwnerUniqueId: String,
    val courtOrderNumber: String,
    val reason: String,
    val status: String = "PENDING",
    val requestedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "court_orders")
data class CourtOrder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val orderNumber: String,
    val courtName: String,
    val details: String,
    val recordId: Int,
    val mandatedNewOwnerName: String?,
    val mandatedNewOwnerUniqueId: String?,
    val mandatedCharge: Double?,
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
    val recordId: Int,
    val transactionType: String,
    val payload: String,
    val nonce: Long
)

@Entity(tableName = "property_valuations")
data class PropertyValuation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val propertyName: String,
    val surveyorName: String,
    val zoneClassification: String,
    val regionalGuidelineRate: Double,
    val landAreaSqFt: Double,
    val developmentalPremiumMultiplier: Double,
    val overallAssessedValue: Double,
    val blockchainSealHash: String,
    val createdTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "secure_vault_records")
data class SecureVaultRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val ownerUid: String,
    val docType: String,
    val docTitle: String,
    val encryptedData: String,
    val iv: String,
    val createdTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "business_entities")
data class BusinessEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: String,
    val registrationNumber: String?,
    val ownerUid: String,
    val ownerName: String,
    val sector: String,
    val status: String,
    val incorporationDate: Long = System.currentTimeMillis(),
    val isStartupIndiaRecognized: Boolean = false,
    val complianceScore: Int = 100
)

@Entity(tableName = "seed_ideas")
data class SeedIdea(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val creatorUid: String,
    val title: String,
    val industry: String,
    val encryptedConcept: String,
    val iv: String,
    val ipProtectionStatus: String,
    val blockchainSeal: String,
    val createdTimestamp: Long = System.currentTimeMillis(),
    val isPrivate: Boolean = true
)

@Entity(tableName = "business_compliance_logs")
data class BusinessComplianceLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val businessId: Int,
    val complianceType: String,
    val status: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)
