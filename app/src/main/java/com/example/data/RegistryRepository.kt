package com.example.data

import kotlinx.coroutines.flow.Flow

class RegistryRepository(private val registryDao: RegistryDao) {

    // Registry Records
    val allRecords: Flow<List<RegistryRecord>> = registryDao.getAllRecords()
    suspend fun getRecordById(id: Int) = registryDao.getRecordById(id)
    fun getRecordsByOwner(uid: String) = registryDao.getRecordsByOwner(uid)
    suspend fun insertRecord(record: RegistryRecord) = registryDao.insertRecord(record)
    suspend fun updateRecord(record: RegistryRecord) = registryDao.updateRecord(record)

    // Change Requests
    val allChangeRequests: Flow<List<OwnershipChangeRequest>> = registryDao.getAllChangeRequests()
    suspend fun insertChangeRequest(request: OwnershipChangeRequest) = registryDao.insertChangeRequest(request)
    suspend fun updateChangeRequest(request: OwnershipChangeRequest) = registryDao.updateChangeRequest(request)

    // Court Orders
    val allCourtOrders: Flow<List<CourtOrder>> = registryDao.getAllCourtOrders()
    suspend fun getCourtOrderByNumber(orderNumber: String) = registryDao.getCourtOrderByNumber(orderNumber)
    suspend fun insertCourtOrder(courtOrder: CourtOrder) = registryDao.insertCourtOrder(courtOrder)
    suspend fun updateCourtOrder(courtOrder: CourtOrder) = registryDao.updateCourtOrder(courtOrder)

    // Blockchain
    val allBlockchainBlocks: Flow<List<BlockchainBlock>> = registryDao.getAllBlockchainBlocks()
    suspend fun insertBlockchainBlock(block: BlockchainBlock) = registryDao.insertBlockchainBlock(block)
    suspend fun getLatestBlockchainBlock() = registryDao.getLatestBlockchainBlock()

    // Valuations
    val allValuations: Flow<List<PropertyValuation>> = registryDao.getAllValuations()
    suspend fun insertValuation(valuation: PropertyValuation) = registryDao.insertValuation(valuation)

    // --- NEW REPOSITORY METHODS ---

    // Employee Auth
    suspend fun getEmployeeById(id: String) = registryDao.getEmployeeById(id)
    suspend fun insertEmployee(employee: Employee) = registryDao.insertEmployee(employee)

    // Audit Logs
    val allAuditLogs: Flow<List<AuditLog>> = registryDao.getAllAuditLogs()
    suspend fun insertAuditLog(log: AuditLog) = registryDao.insertAuditLog(log)

    // Secure Vault
    fun getVaultRecordsByOwner(uid: String) = registryDao.getVaultRecordsByOwner(uid)
    suspend fun insertVaultRecord(record: SecureVaultRecord) = registryDao.insertVaultRecord(record)

    // Helper for automated auditing
    suspend fun logAction(
        userId: String,
        actionType: String,
        entityType: String,
        entityId: String,
        details: String
    ) {
        val log = AuditLog(
            userId = userId,
            actionType = actionType,
            entityType = entityType,
            entityId = entityId,
            details = details,
            ipAddress = "127.0.0.1", // In a real app, get from network
            deviceId = "ANDROID-DEVICE-ID", // In a real app, get from system
            checksum = java.util.UUID.randomUUID().toString() // Simplified checksum
        )
        insertAuditLog(log)
    }
}
