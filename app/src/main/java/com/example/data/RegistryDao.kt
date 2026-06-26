package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RegistryDao {
    @Query("SELECT * FROM registries")
    fun getAllRegistries(): Flow<List<Registry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRegistry(registry: Registry)

    @Update
    suspend fun updateRegistry(registry: Registry)

    @Query("SELECT * FROM mfa_user_accounts WHERE uniqueId = :uid")
    suspend fun getUserAccount(uid: String): MfaUserAccount?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserAccount(account: MfaUserAccount)

    // --- EMPLOYEE & AUTH ---
    @Query("SELECT * FROM employees WHERE employeeId = :id")
    suspend fun getEmployee(id: String): Employee?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployee(employee: Employee)

    // --- PROPOSALS ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProposal(proposal: ActionProposal)

    @Query("SELECT * FROM action_proposals WHERE checkerId IS NULL")
    fun getPendingProposals(): Flow<List<ActionProposal>>

    // --- CIVIL REGISTRY ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCivilEvent(event: CivilRegistry)

    @Query("SELECT * FROM civil_registries ORDER BY timestamp DESC")
    fun getAllCivilEvents(): Flow<List<CivilRegistry>>

    // --- CERTIFICATES ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCertificate(certificate: IssuedCertificate)

    @Query("SELECT * FROM issued_certificates WHERE userId = :userId")
    fun getCertificatesForUser(userId: String): Flow<List<IssuedCertificate>>

    // --- IDENTITY VAULT ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIdentityDocument(doc: IdentityDocument)

    @Query("SELECT * FROM identity_documents WHERE userId = :userId")
    fun getIdentityDocuments(userId: String): Flow<List<IdentityDocument>>

    // --- GST ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGstLedger(ledger: GstLedger)

    @Query("SELECT * FROM gst_ledgers WHERE businessId = :businessId")
    fun getGstLedgers(businessId: Int): Flow<List<GstLedger>>

    // --- SCHEMES ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchemeApplication(app: SchemeApplication)

    @Query("SELECT * FROM schemes_applications WHERE citizenId = :citizenId")
    fun getMySchemeApplications(citizenId: String): Flow<List<SchemeApplication>>

    // --- STUDENT HUB ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudentProject(project: StudentProject)

    @Query("SELECT * FROM student_projects WHERE studentId = :studentId")
    fun getStudentProjects(studentId: String): Flow<List<StudentProject>>

    // --- BUSINESS ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBusiness(business: BusinessEntity)

    @Query("SELECT * FROM business_entities WHERE ownerUid = :ownerUid")
    fun getBusinesses(ownerUid: String): Flow<List<BusinessEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeedIdea(idea: SeedIdea)

    @Query("SELECT * FROM seed_ideas WHERE creatorUid = :uid")
    fun getSeedIdeas(uid: String): Flow<List<SeedIdea>>

    // --- AUDIT ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLog)

    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllAuditLogs(): Flow<List<AuditLog>>

    // --- BLOCKCHAIN ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlock(block: BlockchainBlock)

    @Query("SELECT * FROM blockchain_ledger ORDER BY blockIndex DESC LIMIT 1")
    suspend fun getLastBlock(): BlockchainBlock?
}
