package com.example.data

import kotlinx.coroutines.flow.Flow
import java.security.MessageDigest

class RegistryRepository(private val registryDao: RegistryDao) {
    val allRegistries: Flow<List<Registry>> = registryDao.getAllRegistries()
    val pendingProposals: Flow<List<ActionProposal>> = registryDao.getPendingProposals()
    val allAuditLogs: Flow<List<AuditLog>> = registryDao.getAllAuditLogs()

    suspend fun insertRegistry(registry: Registry) = registryDao.insertRegistry(registry)
    suspend fun updateRegistry(registry: Registry) = registryDao.updateRegistry(registry)
    suspend fun getUserAccount(uid: String) = registryDao.getUserAccount(uid)
    suspend fun insertUserAccount(account: MfaUserAccount) = registryDao.insertUserAccount(account)

    // --- EMPLOYEE & AUTH ---
    suspend fun getEmployee(id: String) = registryDao.getEmployee(id)
    suspend fun insertEmployee(employee: Employee) = registryDao.insertEmployee(employee)

    // --- PROPOSALS ---
    suspend fun insertProposal(proposal: ActionProposal) = registryDao.insertProposal(proposal)

    // --- CIVIL REGISTRY ---
    suspend fun insertCivilEvent(event: CivilRegistry) = registryDao.insertCivilEvent(event)
    fun getAllCivilEvents(): Flow<List<CivilRegistry>> = registryDao.getAllCivilEvents()

    // --- CERTIFICATES ---
    suspend fun insertCertificate(certificate: IssuedCertificate) = registryDao.insertCertificate(certificate)
    fun getCertificatesForUser(userId: String): Flow<List<IssuedCertificate>> = registryDao.getCertificatesForUser(userId)

    // --- IDENTITY VAULT ---
    suspend fun insertIdentityDocument(doc: IdentityDocument) = registryDao.insertIdentityDocument(doc)
    fun getIdentityDocuments(userId: String): Flow<List<IdentityDocument>> = registryDao.getIdentityDocuments(userId)

    // --- GST ---
    suspend fun insertGstLedger(ledger: GstLedger) = registryDao.insertGstLedger(ledger)
    fun getGstLedgers(businessId: Int): Flow<List<GstLedger>> = registryDao.getGstLedgers(businessId)

    // --- SCHEMES ---
    suspend fun insertSchemeApplication(app: SchemeApplication) = registryDao.insertSchemeApplication(app)
    fun getMySchemeApplications(citizenId: String): Flow<List<SchemeApplication>> = registryDao.getMySchemeApplications(citizenId)

    // --- STUDENT HUB ---
    suspend fun insertStudentProject(project: StudentProject) = registryDao.insertStudentProject(project)
    fun getStudentProjects(studentId: String): Flow<List<StudentProject>> = registryDao.getStudentProjects(studentId)

    // --- BUSINESS ---
    suspend fun insertBusiness(business: BusinessEntity) = registryDao.insertBusiness(business)
    fun getBusinesses(ownerUid: String): Flow<List<BusinessEntity>> = registryDao.getBusinesses(ownerUid)
    suspend fun insertSeedIdea(idea: SeedIdea) = registryDao.insertSeedIdea(idea)
    fun getSeedIdeas(uid: String): Flow<List<SeedIdea>> = registryDao.getSeedIdeas(uid)

    // --- AUDIT LOGGING ---
    suspend fun logAction(userId: String, action: String, entity: String, entityId: String, details: String) {
        val timestamp = System.currentTimeMillis()
        val rawData = "$timestamp|$userId|$action|$entity|$entityId"
        val checksum = MessageDigest.getInstance("SHA-256").digest(rawData.toByteArray()).joinToString("") { "%02x".format(it) }
        
        val log = AuditLog(
            userId = userId,
            actionType = action,
            entityType = entity,
            entityId = entityId,
            details = details,
            ipAddress = "127.0.0.1",
            deviceId = "HARDWARE-TOKEN-001",
            checksum = checksum
        )
        registryDao.insertAuditLog(log)
    }

    // --- BLOCKCHAIN ---
    suspend fun addBlock(type: String, payload: String, recordId: Int = 0) {
        val lastBlock = registryDao.getLastBlock()
        val prevHash = lastBlock?.hash ?: "0000000000000000"
        val index = (lastBlock?.blockIndex ?: 0) + 1
        
        // Simple Proof of Work simulation
        var nonce = 0L
        var hash = ""
        val target = "0000"
        
        while (!hash.startsWith(target)) {
            nonce++
            val data = "$index$prevHash$payload$nonce"
            hash = MessageDigest.getInstance("SHA-256").digest(data.toByteArray()).joinToString("") { "%02x".format(it) }
        }
        
        val block = BlockchainBlock(
            blockIndex = index,
            previousHash = prevHash,
            hash = hash,
            transactionType = type,
            payload = payload,
            nonce = nonce
        )
        registryDao.insertBlock(block)
    }
}
