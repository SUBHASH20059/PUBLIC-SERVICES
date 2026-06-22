package com.example.data

import kotlinx.coroutines.flow.Flow

class RegistryRepository(private val registryDao: RegistryDao) {

    // Registry Records
    val allRecords: Flow<List<RegistryRecord>> = registryDao.getAllRecords()
    suspend fun insertRecord(record: RegistryRecord) = registryDao.insertRecord(record)

    // GST Module
    suspend fun getGstProfile(businessId: Int) = registryDao.getGstProfile(businessId)
    suspend fun insertGstProfile(profile: GstProfile) = registryDao.insertGstProfile(profile)
    fun getInvoices(gstin: String) = registryDao.getInvoices(gstin)
    suspend fun insertInvoice(invoice: InvoiceLedger) = registryDao.insertInvoice(invoice)

    // Zero-Trust Employee System
    suspend fun getEmployeeById(id: String) = registryDao.getEmployeeById(id)
    suspend fun insertEmployee(employee: Employee) = registryDao.insertEmployee(employee)
    fun getPendingProposals() = registryDao.getPendingProposals()
    suspend fun insertProposal(proposal: ActionProposal) = registryDao.insertProposal(proposal)
    suspend fun updateProposal(proposal: ActionProposal) = registryDao.updateProposal(proposal)

    // Civil Registries
    val allCivilEvents: Flow<List<CivilRegistry>> = registryDao.getAllCivilEvents()
    suspend fun insertCivilEvent(event: CivilRegistry) = registryDao.insertCivilEvent(event)

    // Certificates
    fun getCertificates(userId: String) = registryDao.getCertificates(userId)
    suspend fun insertCertificate(cert: IssuedCertificate) = registryDao.insertCertificate(cert)

    // Welfare Schemes
    fun getSchemeApplications(citizenId: String) = registryDao.getSchemeApplications(citizenId)
    suspend fun insertSchemeApplication(app: SchemeApplication) = registryDao.insertSchemeApplication(app)

    // Audit Logs (Immutable)
    val allAuditLogs: Flow<List<AuditLog>> = registryDao.getAllAuditLogs()
    suspend fun insertAuditLog(log: AuditLog) = registryDao.insertAuditLog(log)

    // Business Module (Retained)
    fun getBusinessesByOwner(uid: String) = registryDao.getBusinessesByOwner(uid)
    suspend fun insertBusiness(business: BusinessEntity) = registryDao.insertBusiness(business)
    fun getSeedIdeasByCreator(uid: String) = registryDao.getSeedIdeasByCreator(uid)
    suspend fun insertSeedIdea(idea: SeedIdea) = registryDao.insertSeedIdea(idea)

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
            ipAddress = "127.0.0.1",
            deviceId = "SECURE-DEVICE-TOKEN",
            checksum = java.util.UUID.randomUUID().toString()
        )
        registryDao.insertAuditLog(log)
    }
}
