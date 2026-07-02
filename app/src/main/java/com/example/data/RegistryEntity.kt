package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "registries")
data class Registry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val ownerName: String,
    val uniqueId: String,
    val status: String,
    val department: String, // Department isolation
    val lastModified: Long = System.currentTimeMillis()
)

@Entity(tableName = "employees")
data class Employee(
    @PrimaryKey val employeeId: String,
    val name: String,
    val role: String, // OFFICER, AUDITOR, CLERK, ADMIN
    val department: String, // Department isolation
    val authorizedDeviceId: String,
    val currentTaskCount: Int = 0, // For intelligent routing
    val isActive: Boolean = true
)

@Entity(tableName = "action_proposals")
data class ActionProposal(
    @PrimaryKey(autoGenerate = true) val proposalId: Int = 0,
    val applicationId: Int,
    val department: String, // Department isolation
    val makerId: String,
    val proposedState: String,
    val checkerId: String?,
    val submissionTimestamp: Long = System.currentTimeMillis(),
    val approvalTimestamp: Long?,
    val digitalSignatureHash: String?
)

@Entity(tableName = "audit_logs")
data class AuditLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val userId: String,
    val department: String, // Department isolation
    val actionType: String,
    val entityType: String,
    val entityId: String,
    val details: String,
    val ipAddress: String,
    val deviceId: String,
    val checksum: String
)

// ... (retaining other entities but ensuring they have department where relevant)
