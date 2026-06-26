package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "registries")
data class Registry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val ownerName: String,
    val uniqueId: String,
    val status: String,
    val lastModified: Long = System.currentTimeMillis()
)

@Entity(tableName = "mfa_user_accounts")
data class MfaUserAccount(
    @PrimaryKey val uniqueId: String,
    val name: String,
    val pinHash: String,
    val mfaSecret: String,
    val isVerified: Boolean = false
)

@Entity(tableName = "employees")
data class Employee(
    @PrimaryKey val employeeId: String,
    val name: String,
    val role: String, // OFFICER, AUDITOR, CLERK, ADMIN
    val department: String,
    val authorizedDeviceId: String,
    val isActive: Boolean = true
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
    val eventType: String, // BIRTH, DEATH, MARRIAGE, DIVORCE
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
    val certificateType: String, // INCOME, CASTE, RESIDENCE, BIRTH, DEATH, MARRIAGE, EDUCATION, BUSINESS
    val cryptographicHash: String,
    val issuingAuthorityId: String,
    val validUntil: Long
)

// --- IDENTITY VAULT (PSEI v2.0) ---
@Entity(tableName = "identity_documents")
data class IdentityDocument(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val documentType: String, // AADHAAR, VOTER_ID, PAN, PASSPORT, DL, etc.
    val documentNumber: String,
    val issuingAuthority: String,
    val encryptedData: String,
    val iv: String,
    val status: String, // VERIFIED, PENDING, EXPIRED
    val issueDate: Long,
    val expiryDate: Long?
)

// --- GST & TAXATION ---
@Entity(tableName = "gst_ledgers")
data class GstLedger(
    @PrimaryKey(autoGenerate = true) val ledgerId: Int = 0,
    val businessId: Int,
    val period: String,
    val itcAvailable: Double,
    val cashBalance: Double,
    val liabilityAmount: Double,
    val status: String // FILED, PENDING
)

// --- WELFARE SCHEMES & DBT ---
@Entity(tableName = "schemes_applications")
data class SchemeApplication(
    @PrimaryKey(autoGenerate = true) val applicationId: Int = 0,
    val schemeId: Int,
    val schemeName: String,
    val citizenId: String,
    val criteriaJson: String,
    val currentStage: String,
    val paymentStatus: String // PENDING, DISBURSED
)

// --- STUDENT INNOVATION HUB ---
@Entity(tableName = "student_projects")
data class StudentProject(
    @PrimaryKey(autoGenerate = true) val projectId: Int = 0,
    val studentId: String,
    val title: String,
    val description: String,
    val mentorId: String?,
    val grantAmount: Double?,
    val status: String // SUBMITTED, APPROVED, FUNDED
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

// --- BUSINESS MODULE (SEED TO MNC) ---
@Entity(tableName = "business_entities")
data class BusinessEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: String, // STARTUP, PVT_LTD, PUB_LTD, MNC
    val registrationNumber: String?,
    val ownerUid: String,
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

// --- PREVIOUS MODULES (RETAINED) ---
@Entity(tableName = "ownership_change_requests")
data class OwnershipChangeRequest(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val recordId: Int,
    val requestedNewOwnerUniqueId: String,
    val courtOrderNumber: String,
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
    val isExecuted: Boolean = false
)

@Entity(tableName = "blockchain_ledger")
data class BlockchainBlock(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val blockIndex: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val previousHash: String,
    val hash: String,
    val transactionType: String,
    val payload: String,
    val nonce: Long
)
