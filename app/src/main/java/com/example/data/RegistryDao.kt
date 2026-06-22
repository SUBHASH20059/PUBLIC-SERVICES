package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RegistryDao {
    // Registry Records
    @Query("SELECT * FROM registry_records ORDER BY createdTimestamp DESC")
    fun getAllRecords(): Flow<List<RegistryRecord>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: RegistryRecord)
    @Update
    suspend fun updateRecord(record: RegistryRecord)

    // GST Module
    @Query("SELECT * FROM gst_profiles WHERE businessId = :businessId")
    suspend fun getGstProfile(businessId: Int): GstProfile?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGstProfile(profile: GstProfile)
    @Query("SELECT * FROM invoice_ledger WHERE supplierGstin = :gstin OR recipientGstin = :gstin")
    fun getInvoices(gstin: String): Flow<List<InvoiceLedger>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: InvoiceLedger)

    // Employee System
    @Query("SELECT * FROM employees WHERE employeeId = :id")
    suspend fun getEmployeeById(id: String): Employee?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployee(employee: Employee)
    @Query("SELECT * FROM action_proposals WHERE checkerId IS NULL")
    fun getPendingProposals(): Flow<List<ActionProposal>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProposal(proposal: ActionProposal)
    @Update
    suspend fun updateProposal(proposal: ActionProposal)

    // Civil Registries
    @Query("SELECT * FROM civil_registries ORDER BY timestamp DESC")
    fun getAllCivilEvents(): Flow<List<CivilRegistry>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCivilEvent(event: CivilRegistry)

    // Certificates
    @Query("SELECT * FROM issued_certificates WHERE userId = :userId")
    fun getCertificates(userId: String): Flow<List<IssuedCertificate>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCertificate(cert: IssuedCertificate)

    // Welfare Schemes
    @Query("SELECT * FROM schemes_applications WHERE citizenId = :citizenId")
    fun getSchemeApplications(citizenId: String): Flow<List<SchemeApplication>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchemeApplication(app: SchemeApplication)

    // Audit Logs
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllAuditLogs(): Flow<List<AuditLog>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLog)
    
    // Business Module (Retained)
    @Query("SELECT * FROM business_entities WHERE ownerUid = :uid")
    fun getBusinessesByOwner(uid: String): Flow<List<BusinessEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBusiness(business: BusinessEntity)
    @Query("SELECT * FROM seed_ideas WHERE creatorUid = :uid")
    fun getSeedIdeasByCreator(uid: String): Flow<List<SeedIdea>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeedIdea(idea: SeedIdea)
}
