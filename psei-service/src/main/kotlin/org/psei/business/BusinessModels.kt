package org.psei.business

import kotlinx.serialization.Serializable

@Serializable
data class Business(
    val id: Long? = null,
    val legalName: String,
    val registrationNumber: String,
    val type: String? = null,
    val incorporationDate: String? = null,
    val status: String? = "ACTIVE",
    val primaryContact: String? = null,
    val addresses: Map<String, String> = emptyMap()
)

@Serializable
data class Ownership(
    val id: Long? = null,
    val businessId: Long,
    val ownerId: String,
    val ownerType: String, // PERSON or ENTITY
    val ownershipPercentage: Double,
    val role: String, // DIRECTOR / SHAREHOLDER
    val effectiveFrom: String? = null,
    val effectiveTo: String? = null
)

@Serializable
data class PersonEntity(
    val id: String,
    val name: String,
    val identifierType: String? = null, // PAN / PASSPORT / GOV_ID
    val identifierValue: String? = null,
    val contact: String? = null
)

@Serializable
data class DocumentMetadata(
    val id: Long? = null,
    val businessId: Long,
    val type: String,
    val filename: String,
    val storagePath: String,
    val uploadedAt: String,
    val uploadedBy: String,
    val encrypted: Boolean = true,
    val metadata: Map<String, String> = emptyMap()
)

@Serializable
data class TaxRecordSummary(
    val businessId: Long,
    val gstNumber: String? = null,
    val lastFilingDate: String? = null,
    val filingStatus: String? = null
)

@Serializable
data class UnifiedIdentity(
    val userId: String,
    val verified: Boolean = false,
    val linkedBusinesses: List<Long> = emptyList(),
    val linkedProperties: List<String> = emptyList(),
    val linkedDocs: List<Long> = emptyList()
)

@Serializable
data class RoleAssignment(
    val id: Long? = null,
    val userId: String,
    val businessId: Long,
    val role: String, // DIRECTOR/SHAREHOLDER/ACCOUNTANT/EMPLOYEE
    val permissions: Map<String, Boolean> = emptyMap()
)

@Serializable
data class AuditLog(
    val id: Long? = null,
    val objectType: String,
    val objectId: Long,
    val actorId: String,
    val action: String,
    val timestamp: String,
    val details: String? = null
)
