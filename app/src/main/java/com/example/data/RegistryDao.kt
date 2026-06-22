package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RegistryDao {
    // Registry Records
    @Query("SELECT * FROM registry_records ORDER BY createdTimestamp DESC")
    fun getAllRecords(): Flow<List<RegistryRecord>>

    @Query("SELECT * FROM registry_records WHERE id = :id")
    suspend fun getRecordById(id: Int): RegistryRecord?

    @Query("SELECT * FROM registry_records WHERE ownerUniqueId = :uid")
    fun getRecordsByOwner(uid: String): Flow<List<RegistryRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: RegistryRecord)

    @Update
    suspend fun updateRecord(record: RegistryRecord)

    // Change Requests
    @Query("SELECT * FROM ownership_change_requests ORDER BY requestedTimestamp DESC")
    fun getAllChangeRequests(): Flow<List<OwnershipChangeRequest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChangeRequest(request: OwnershipChangeRequest)

    @Update
    suspend fun updateChangeRequest(request: OwnershipChangeRequest)

    // Court Orders
    @Query("SELECT * FROM court_orders ORDER BY id DESC")
    fun getAllCourtOrders(): Flow<List<CourtOrder>>

    @Query("SELECT * FROM court_orders WHERE orderNumber = :orderNumber")
    suspend fun getCourtOrderByNumber(orderNumber: String): CourtOrder?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourtOrder(courtOrder: CourtOrder)

    @Update
    suspend fun updateCourtOrder(courtOrder: CourtOrder)

    // Blockchain Ledger
    @Query("SELECT * FROM blockchain_ledger ORDER BY blockIndex ASC")
    fun getAllBlockchainBlocks(): Flow<List<BlockchainBlock>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockchainBlock(block: BlockchainBlock)

    @Query("SELECT * FROM blockchain_ledger ORDER BY blockIndex DESC LIMIT 1")
    suspend fun getLatestBlockchainBlock(): BlockchainBlock?

    // Property Valuations
    @Query("SELECT * FROM property_valuations ORDER BY createdTimestamp DESC")
    fun getAllValuations(): Flow<List<PropertyValuation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertValuation(valuation: PropertyValuation)

    // Employee Auth
    @Query("SELECT * FROM employees WHERE employeeId = :id")
    suspend fun getEmployeeById(id: String): Employee?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployee(employee: Employee)

    // Audit Logs
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllAuditLogs(): Flow<List<AuditLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLog)

    // Secure Vault
    @Query("SELECT * FROM secure_vault_records WHERE ownerUid = :uid")
    fun getVaultRecordsByOwner(uid: String): Flow<List<SecureVaultRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVaultRecord(record: SecureVaultRecord)

    // --- BUSINESS MODULE METHODS ---

    @Query("SELECT * FROM business_entities WHERE ownerUid = :uid")
    fun getBusinessesByOwner(uid: String): Flow<List<BusinessEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBusiness(business: BusinessEntity)

    @Query("SELECT * FROM seed_ideas WHERE creatorUid = :uid")
    fun getSeedIdeasByCreator(uid: String): Flow<List<SeedIdea>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeedIdea(idea: SeedIdea)

    @Query("SELECT * FROM business_compliance_logs WHERE businessId = :businessId ORDER BY timestamp DESC")
    fun getComplianceLogs(businessId: Int): Flow<List<BusinessComplianceLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComplianceLog(log: BusinessComplianceLog)
}
